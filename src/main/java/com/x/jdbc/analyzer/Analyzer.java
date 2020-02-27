package com.x.jdbc.analyzer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.PSSetter;
import com.x.jdbc.RSParser;

/**
 * 
 * 分析
 * @author 
 *
 */
public class Analyzer {
	
	private static final String CONNECT_CLOSE = "关闭连接出错";
    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);

    public static FTable analyze(AnalyzerInfo<?> info) {
        return analyze(info,
        		analyze(info.clazz(), info.mappings(), info), 
                analyze(info.jdbcTemplate(), info.tableName()));
    }
    
    /**
     * 对比双方 去掉多余的属性
     * @param dbtable
     * @param jtable
     */
    public static FTable analyze(AnalyzerInfo<?> info, JTable jtable, DBTable dbtable) {
    	FTable fTable = new FTable();
    	fTable.jTable = jtable;
    	fTable.clazz = info.clazz();
    	fTable.mappings = info.mappings();
    	fTable.maxPreparedStmtCount = info.jdbcTemplate().maxPreparedStmtCount();
    	
    	if(dbtable != null) {
    		fTable.tableName = dbtable.tableName;

		    for (DBColumn primaryKey : dbtable.primaryKeys) {
		        JColumn jcolumn = jtable.getJColumn(primaryKey.name);
		        if(jcolumn != null) {
		            fTable.primaryKeys.add(new FColumn(primaryKey, jcolumn));
		        } else {
		            logger.warn("NONE MAPPING PRIMARYKEY TO {}.{}", dbtable.tableName, primaryKey.name);
		        }
            }
    		
		    for (DBIndex uniqueIndex : dbtable.uniqueIndexes) {
		        FIndex fIndex = new FIndex(uniqueIndex.keyNmae, uniqueIndex.nonUnique);
		        for (DBColumn indexColumn : uniqueIndex.columns) {
		            JColumn jcolumn = jtable.getJColumn(indexColumn.name);
		            if(jcolumn != null) {
                        fIndex.columns.add(new FColumn(indexColumn, jcolumn));
                    } else {
                        logger.warn("NONE MAPPING UNIQUE INDEX TO {}.{}", dbtable.tableName, indexColumn.name);
                    }
                }
		        fTable.uniqueIndexs.add(fIndex);
            }
		    
		    for (DBIndex index : dbtable.indexes) {
		        FIndex fIndex = new FIndex(index.keyNmae, index.nonUnique);
		        for (DBColumn indexColumn : index.columns) {
		            JColumn jcolumn = jtable.getJColumn(indexColumn.name);
		            if(jcolumn != null) {
		                fIndex.columns.add(new FColumn(indexColumn, jcolumn));
		            } else {
		                logger.warn("NONE MAPPING INDEX TO {}.{}", dbtable.tableName, indexColumn.name);
		            }
		        }
		        fTable.indexs.add(fIndex);
		    }

    		for(String dbcolumnname : dbtable.columns.keySet()) {
    			JColumn jcolumn = jtable.getJColumn(dbcolumnname);
    			if(jcolumn != null) {
    				FColumn fcolumn = new FColumn(dbtable.getDBColumn(dbcolumnname), jcolumn);
    				fTable.columns.add(fcolumn);
    				fTable.columnMap.put(dbcolumnname, fcolumn);
    			} else {
    				logger.debug("{} NONE MAPPING TO {}.{}", jtable.clazz.getSimpleName(), dbtable.tableName, dbtable.getDBColumn(dbcolumnname).name);
    			}
    		}

    		Collections.sort(fTable.columns, new Comparator<FColumn>() {
    			@Override
    			public int compare(FColumn o1, FColumn o2) {
    				return o1.dbColumn.index - o2.dbColumn.index;
    			}
    		});
    	}
    	
    	for (Map.Entry<String, JColumn> entry : jtable.columns.entrySet()) {
    		JColumn jcolumn = entry.getValue();
    		//field type codec to field name codec
			if(info.hasCodec(jcolumn.name)) {
                fTable.codecs.put(jcolumn.name, info.getCodec(jcolumn.name));
            } else if(info.hasCodec(jcolumn.type)) {
            	fTable.codecs.put(jcolumn.name, info.getCodec(jcolumn.field));
            }
    		
    		//多出来的java字段, 特殊SQL可能需要用到(多表联合查询)
    		String dbcolumname = entry.getKey();
			if(fTable.columnMap.get(dbcolumname) == null) {
			    DBColumn dbColumn = dbtable.getDBColumn(dbcolumname);
				fTable.columnMap.put(dbcolumname, new FColumn(dbColumn, jtable.getJColumn(dbcolumname)));
			}
		}
    	fTable.codecs.setTypeParser(info.typeParser());
    	return fTable;
	}
    
    /**
     * @param clazz
     * @param mappings  JAVA_FIELD_NAME <> DB_COLUMN_NAME
     * @param map 
     * @return
     */
    public static JTable analyze(Class<?> clazz, Map<String, String> mappings, AnalyzerInfo<?> info) {
	    Class<?> orign = clazz;
    	JTable table = new JTable(clazz);
    	
    	while(clazz != null) {
    		Field[] fields = clazz.getDeclaredFields();
    		for (Field field : fields) {
				int modifiers = field.getModifiers();
				if(Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
					continue;
				}
				JColumn column = new JColumn();
				column.field = field;
				column.name = field.getName();
				column.type = field.getType();
				column.isPrivate = Modifier.isPrivate(modifiers) || (!orign.equals(clazz));
				
				//DB_COLUMN_NAME as key
				table.addJColumn(mappings.get(column.name) == null ? column.name : mappings.get(column.name), column);
			}
    		
    		Method[] methods = clazz.getDeclaredMethods();
    		for (Method method : methods) {
    		    String name = method.getName();
                if((name.startsWith("is") || name.startsWith("get")) && method.getParameterTypes().length == 0) {
    		        table.getter.put(name.toLowerCase(), name);
    		    }
            }
    		
    		clazz = clazz.getSuperclass();
    	}
    	return table;
    }
	
    public static DBTable analyze(JDBCTemplate jdbcTemplate, final String tableName) {
    	if(tableName == null) return null;
    	
		String sql = "select * from " + tableName + " where 1 = 0;";
		DBTable table = jdbcTemplate.fetch(sql, PSSetter.NONE, new RSParser<DBTable>() {
			@Override
			public DBTable parse(ResultSet rs) throws SQLException {
				ResultSetMetaData rsmd = rs.getMetaData();
	            int count = rsmd.getColumnCount();
	            DBTable table = new DBTable(tableName);
	            for (int i = 1; i <= count; i++) {
	            	DBColumn column = new DBColumn();
	            	column.index = i;
	            	column.name = rsmd.getColumnLabel(i);
	            	column.type = rsmd.getColumnType(i);
					table.addDBColumn(column);
				}
				return table;
			}
		});
		
		Connection conn = null;
        try {
            conn = jdbcTemplate.dataSource.getConnection();
            ResultSet primaryKeys = conn.getMetaData().getPrimaryKeys(null, null, tableName);
            while(primaryKeys.next()) {
            	table.primaryKeys.add(table.getDBColumn(primaryKeys.getString("COLUMN_NAME")));
            }
            ResultSet uniqueIndexs = conn.getMetaData().getIndexInfo(null, null, tableName, false, false);
            while(uniqueIndexs.next()) {
                table.addDBIndex(uniqueIndexs.getString("INDEX_NAME"), uniqueIndexs.getBoolean("NON_UNIQUE"), table.getDBColumn(uniqueIndexs.getString("COLUMN_NAME")));
            }
            return table;
        } catch (SQLException e) {
        	logger.error("Table compile wrong", e);
        } finally {
            close(conn);
        }
        return null;
	}
	
    public static void close(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error(CONNECT_CLOSE, e);
        }
    }
    
}

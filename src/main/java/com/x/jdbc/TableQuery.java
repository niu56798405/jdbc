package com.x.jdbc;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.x.jdbc.analyzer.Analyzer;
import com.x.jdbc.analyzer.AnalyzerInfo;
import com.x.jdbc.analyzer.FTable;
import com.x.jdbc.codec.FieldCodec;
import com.x.jdbc.codec.Json;
import com.x.jdbc.codec.JsonCodec;
import com.x.jdbc.compiler.SQLCompiler;
import com.x.jdbc.compiler.SQLFactory;
import com.x.jdbc.compiler.SQLGenerator;
import com.x.jdbc.sequal.ScheduledSQL;
import com.x.jdbc.sequal.Sequal;


/**
 * 一一对应一张数据库表与一个java实体类(Entity)
 * 绑定一个数据库类型(每个类型对应一个连接池)
 * 
 * Java Entity
 *  对数据的操作 都会定时 批量执行落地
 *
 * 
 * @author 
 */
public class TableQuery<T> {
	
    private transient AnalyzerInfo<T> info;
    
    private String tableName;
    //final compiled sqls
	private SQL<T>[] sqls;

	public static final int    ALL = 127;
	public static final int  QUERY = 1;
	public static final int INSERT = 2;
	public static final int DELETE = 4;
	public static final int UPDATE = 8;
	public static final int INSTUP = 16;//insert update
	
	private static Set<TableQuery<?>> queries = new HashSet<>(); 
	
	private int without;
	
	public TableQuery(Class<T> clazz) {	
    	this.info = new AnalyzerInfo<T>(clazz);
		queries.add(this);
	}

	/**
	 * 不自动生成哪些类型的SQL
	 * Demo:
	 *     query.without(TypeQuery.QUERY | TypeQuery.INSERT | TypeQuery.DELETE | TypeQuery.UPDATE)
	 * @param without
	 * @return
	 */
	public TableQuery<T> without(int without) {
	    this.without = without;
	    return this;
	}
	
	/**
	 * 联合查询
	 * @param fieldName
	 * @param tableName
	 * @return 对应该表的TypeQuery
	 */
	public TableQuery<T> associate(String fieldName, String tableName) {
		//FIXME
		return this;
	}
	
	/**
	 * 映射Java实体字段与数据库字段
	 * @param fieldName Java实体字段
	 * @param columnName 数据库字段
	 * @return
	 */
	public TableQuery<T> mapping(String fieldName, String columnName) {
	    info.mapping(fieldName, columnName);
		return this;
	}
	
	/**
	 * 绑定datasource类型
	 * @param type
	 * @return
	 */
	public TableQuery<T> binding(Enum<?> type) {
	    info.binding(type);	
		return this;
	}
	
	public TableQuery<T> binding(Enum<?> type, String tableName) {
	    this.tableName = tableName;	    
	    info.binding(type, tableName);
		return this;
	}
	
	public TableQuery<T> binding(JDBCTemplate jdbcTemplate, String tableName) {
        this.tableName = tableName;
        
        info.binding(jdbcTemplate, tableName);
        return this;
    }
	
	public TableQuery<T> binding(JDBCTemplate jdbcTemplate) {
	    info.binding(jdbcTemplate);
        return this;
    }
    
	public TableQuery<T> binding(int idx, String sql) {
	    return binding(idx, Sequal.origin(sql));
	}
	
	public TableQuery<T> binding(int idx, Sequal sequal) {
	    info.binding(idx, sequal);
	    return this;
	}
	
	public TableQuery<T> binding(String fieldName, FieldCodec<?, ?> codec) {
	    info.binding(fieldName, codec);
	    return this;
	}
	
	/**
	 * 用于collection ，map，或者对象 等非基本类型  字段 绑定json 编解码
	 * @param fidldName
	 * @return
	 */
	public TableQuery<T> bindingJson(String fidldName){
	    try {
			Field declaredField = info.clazz().getDeclaredField(fidldName);
			info.binding(fidldName, JsonCodec.buildCodec(declaredField.getType(), declaredField.getGenericType()));
		}catch (Exception e) {		
			e.printStackTrace();
		}
	    return this;
	}
	
	public TableQuery<T> binding(TypeParser<T> typeParser) {
	    info.binding(typeParser);
	    return this;
	}
	
	public TableQuery<T> binding(Class<?> fieldType, FieldCodec<?, ?> codec) {
        info.binding(fieldType, codec);
        return this;
    }
	
	public SQL<T> load(int idx) {
		return compile().check(sqls[idx]);
	}
	
	private SQL<T> qryall;
	public List<T> fetchall() {
		return compile().check(qryall).fetchMany(PSSetter.NONE);
	}
	
	public List<T> fetchMany(Map<String, Object> param) {
		return compile().check(qryall).fetchMany(param);
	}
	
	public List<T> fetchMany(String fieldName, Object value) {
	
		return compile().check(qryall).fetchMany(fieldName, value); 
	}

	
	private SQL<T> qrykey;
	public T fetchoneByKey(Object key) {
		return compile().check(qrykey).fetchOneByKey(key);
	}
	public T fetchoneByKey(PSSetter key) {
	    return compile().check(qrykey).fetchOneByKey(key);
	}
	
	private SQL<T> insert;
	public boolean insert(T obj) {
		return compile().check(insert).update(obj);
	}
	
	private SQL<T> instup;
    public boolean instup(T obj) {
        return compile().check(instup).update(obj);
    }
    public boolean instupBatch(T[] objs) {
        return compile().check(instup).updateBatch(objs);
    }
    public boolean instupBatch(Collection<T> objs) {
        return compile().check(instup).updateBatch(objs);
    }
	

	
	private SQL<T> delete;
	public boolean delete(T obj) {
	    return compile().check(delete).update(obj);
	}
	
    private SQL<T> check(SQL<T> sql) {
        if(tableName == null) {
            throw new IllegalArgumentException("None binding table");
        }
        if(sql == null) {
            throw new IllegalArgumentException("Current SQL is None, Make sure the table:[" + tableName + "] has primary key and SQL not int the 'without' settings");
        }
        return sql;
    }
    
    public TableQuery<T> compile() {
        if(this.info == null) return this;
        synchronized(this.info) {
            if(this.info == null) return this;//double check
                       
            try {
            	bindingJsonField();
            	
                FTable ftable = Analyzer.analyze(info);
                SQLFactory<T> factory = info.buildFactory();
                
                if((without & QUERY) == 0) {
                    this.qryall = SQLGenerator.genQuerySQL(factory, ftable);
                    this.qrykey = SQLGenerator.genQryKeySQL(factory, ftable, qryall);
                }
                	
                if((without & INSERT) == 0)
                    this.insert = SQLGenerator.genInsertSQL(factory, ftable);
                
                if((without & INSTUP) == 0)
                    this.instup = SQLGenerator.genInstupSQL(factory, ftable, insert);

                if((without & DELETE) == 0)
                    this.delete = SQLGenerator.genDeleteSQL(factory, ftable);

                this.sqls = SQLCompiler.compile(info.sequals(), factory, ftable);

                this.info = null;
                //定时执行
                registerTimer();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }    
        }
        return this;
    }
    
    public TableQuery<T> registerTimer(){
    	Configuration.getTimer().scheduleAtFixedRate(()-> execSql(), 5, 5, TimeUnit.SECONDS);    	
    	return this;
    }
    
    private void  bindingJsonField() {
        Field[] fields = info.clazz().getDeclaredFields();
        for(Field field : fields){
        	Json annotation = field.getAnnotation(Json.class);
        	Class<?> type = field.getType();
			if (annotation != null || Map.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type)) {
				bindingJson(field.getName());
			}
        
        }
	}
    
    
    private void execSql(){
    	((ScheduledSQL<T>)insert).persistence();
    	((ScheduledSQL<T>)instup).persistence();
    	((ScheduledSQL<T>)delete).persistence();	
    }
    
    
    public static void doCompile() {
        queries.forEach(TableQuery::compile);
    }
    
    public static void shutdown() {
        Configuration.shutdown();
    }

}

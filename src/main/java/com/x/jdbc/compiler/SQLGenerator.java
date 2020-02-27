package com.x.jdbc.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.x.jdbc.RSParser;
import com.x.jdbc.SQL;
import com.x.jdbc.analyzer.FColumn;
import com.x.jdbc.analyzer.FTable;
import com.x.jdbc.sequal.AbtSQL;


/**
 * 
 * 生成默认SQL
 * @author 
 * 
 */
public class SQLGenerator {
    
    public static <T> SQL<T> genInsertSQL(SQLFactory<T> factory, FTable ftable) throws Exception {
        StringBuilder insertPre = new StringBuilder();
        StringBuilder insertEnd = new StringBuilder();
        
        int index = 1;
        for (FColumn fColumn : ftable.columns) {
            insertPre.append((index == 1) ? "" : ",").append(fColumn.dbColumn.columnNameForQuery());
            insertEnd.append((index == 1) ? "" : ",").append("?");
            ++index;
        }
        
        StringBuilder sql = new StringBuilder().append("INSERT INTO ").append(ftable.tableName).append(" (").append(insertPre).append(") VALUES (").append(insertEnd).append(")");
        return factory.newSQL(sql.toString(), ftable.primaryKeys.size(), ftable.batchLimit(ftable.columns.size()), CodeGenerator.makeSetter(ftable, ftable.columns), null);
    }
    
    public static <T> SQL<T> genInstupSQL(SQLFactory<T> factory, FTable ftable, SQL<T> insert) throws Exception {
        if(ftable.hasOnlyOneUniqueIndex() && ftable.hasColumnNotInUniqueIndex()) {
            AbtSQL<T> insertSQL = ((AbtSQL<T>) (insert == null ? genInsertSQL(factory, ftable) : insert));
            StringBuilder updatePart = new StringBuilder();
            int index = 1;
            for (FColumn fColumn : ftable.columns) {
                if(ftable.primaryKeys.contains(fColumn)) continue;
                updatePart.append((index == 1) ? "" : ",").append(fColumn.dbColumn.columnNameForQuery()).append("=VALUES(").append(fColumn.dbColumn.columnNameForQuery()).append(")");
                ++index;
            }

            StringBuilder sql = new StringBuilder().append(insertSQL.getSql()).append(" ON DUPLICATE KEY UPDATE ").append(updatePart);
            return factory.newSQL(sql.toString(), ftable.primaryKeys.size(), ftable.batchLimit(ftable.columns.size()), insertSQL.getSetter(), null);
        }
        return null;
    }
    
    public static <T> SQL<T> genUpdateSQL(SQLFactory<T> factory, FTable ftable, List<FColumn> whereColumns) throws Exception {
        if(!whereColumns.isEmpty()) {
            List<FColumn> fColumns = new ArrayList<FColumn>();

            StringBuilder updatePart = new StringBuilder();
            int index = 1;
            for (FColumn fColumn : ftable.columns) {
                if(whereColumns.contains(fColumn)) continue;
                updatePart.append((index == 1) ? "" : ",").append(fColumn.dbColumn.columnNameForQuery()).append("=?");
                fColumns.add(fColumn);
                ++ index;
            }
            
            StringBuilder wherePart = new StringBuilder();
            for (int i = 0; i < whereColumns.size(); i++) {
                FColumn fColumn = whereColumns.get(i);
                wherePart.append((i == 0) ? "" : " AND ").append(fColumn.dbColumn.columnNameForQuery()).append("=?");
                fColumns.add(fColumn);
                ++index;
            }
            
            StringBuilder sql = new StringBuilder()
                    .append("UPDATE ").append(ftable.tableName)
                    .append(" SET ").append(updatePart)
                    .append(" WHERE ").append(wherePart);
            
            return factory.newSQL(sql.toString(), whereColumns.size(), ftable.batchLimit(ftable.columns.size() + whereColumns.size()), CodeGenerator.makeSetter(ftable, fColumns), null);
        }
        return null;
    }
    
    public static <T> SQL<T> genUpdateSQL(SQLFactory<T> factory, FTable ftable) throws Exception {
        return genUpdateSQL(factory, ftable, ftable.primaryKeys);
    }
    
    public static <T> SQL<T> genDeleteSQL(SQLFactory<T> factory, FTable ftable) throws Exception {
        return genDeleteSQL(factory, ftable, ftable.primaryKeys);
    }
    
    public static <T> SQL<T> genDeleteSQL(SQLFactory<T> factory, FTable ftable, List<FColumn> whereColumns) throws Exception {
        if(!whereColumns.isEmpty()) {
            List<FColumn> fColumns = new ArrayList<FColumn>();
            StringBuilder sql = new StringBuilder().append("DELETE FROM ").append(ftable.tableName).append(" WHERE ");
            for (int i = 0; i < whereColumns.size(); i++) {
                FColumn fColumn = whereColumns.get(i);
                sql.append((i == 0) ? "" : " AND ").append(fColumn.dbColumn.columnNameForQuery()).append("=?");
                fColumns.add(fColumn);
            }
            return factory.newSQL(sql.toString(), whereColumns.size(), ftable.batchLimit(whereColumns.size()), CodeGenerator.makeSetter(ftable, fColumns), null);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> SQL<T> genQuerySQL(SQLFactory<T> factory, FTable ftable) throws Exception {
        return genQuerySQL(factory, ftable, Collections.EMPTY_LIST);
    }
    
    public static <T> SQL<T> genQryKeySQL(SQLFactory<T> factory, FTable ftable, SQL<T> queryall) throws Exception {
        return genQuerySQL(factory, ftable, ftable.primaryKeys, ((AbtSQL<T>) queryall).getParser());
    }
    
    public static <T> SQL<T> genQuerySQL(SQLFactory<T> factory, FTable ftable, List<FColumn> whereColumns) throws Exception {
        return genQuerySQL(factory, ftable, whereColumns, null);
    }
    
    public static <T> SQL<T> genQuerySQL(SQLFactory<T> factory, FTable ftable, List<FColumn> whereColumns, RSParser<T> parser) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        for (int i = 0; i < ftable.columns.size(); i++) 
            sql.append((i == 0) ? "" : ",").append(ftable.columns.get(i).dbColumn.columnNameForQuery());
        
        sql.append(" FROM ").append(ftable.tableName);
        
        if(!whereColumns.isEmpty()) {
            sql.append(" WHERE ");
            
            for (int i = 0; i < whereColumns.size(); i++) 
                sql.append((i == 0) ? "" : " AND ").append(whereColumns.get(i).dbColumn.columnNameForQuery()).append("=?");
        }
        
		return factory.newSQL(sql.toString(), whereColumns.size(), ftable.batchLimit(whereColumns.size()), null, parser==null?CodeGenerator.makeParser(ftable, ftable.columns):parser);
    }

}

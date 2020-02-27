package com.x.jdbc.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * db table
 * @author 
 *
 */
public class DBTable {
	String tableName;
	List<DBColumn> primaryKeys;
	List<DBIndex> uniqueIndexes;
	List<DBIndex> indexes;
	
	Map<String, DBColumn> columns;
	
	public DBTable(String name) {
		this.tableName = name;
		this.primaryKeys = new ArrayList<DBColumn>();
		this.uniqueIndexes = new ArrayList<DBIndex>();
		this.indexes = new ArrayList<DBIndex>();
		this.columns = new HashMap<String, DBColumn>();
	}
	public void addDBIndex(String keyName, boolean nonUnique, DBColumn column) {
	    if(!nonUnique) {
	        addDBIndex0(uniqueIndexes, keyName, nonUnique, column);
	    }
	    addDBIndex0(indexes, keyName, nonUnique, column);
	}
    protected void addDBIndex0(List<DBIndex> uniqueIndexes, String keyName, boolean nonUnique, DBColumn column) {
        DBIndex idx = null;
	    for (DBIndex dbIndex : uniqueIndexes) {
            if(dbIndex.keyNmae.equals(keyName)) {
                idx = dbIndex;
                break;
            }
        }
	    if(idx == null) {
	        idx = new DBIndex(keyName, nonUnique);
	        uniqueIndexes.add(idx);
	    }

	    idx.columns.add(column);
    }
	
	public DBColumn getDBColumn(String columnName) {
	    return columns.get(columnName.toLowerCase());
	}
	
	public void addDBColumn(DBColumn dbColumn) {
	    this.columns.put(dbColumn.name.toLowerCase(), dbColumn);
	}
	
}
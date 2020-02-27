package com.x.jdbc.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * analyze final table
 * @author 
 *
 */
public class FTable {
    public String tableName;
    public Class<?> clazz;
	public List<FColumn> primaryKeys = new ArrayList<>();
	public List<FIndex> uniqueIndexs = new ArrayList<>();
	public List<FIndex> indexs = new ArrayList<>();
	public List<FColumn> columns = new ArrayList<>();
	public Map<String, FColumn> columnMap = new HashMap<>();//key column name (lowercase)
	public JTable jTable;
	public FCodecs codecs = new FCodecs();
	public int maxPreparedStmtCount;
	public Map<String, String> mappings;//field name : column name
	
	public boolean hasPrimaryKey() {
	    return this.primaryKeys.size() > 0;
	}
	public boolean hasUniqueIndex() {
	    return this.uniqueIndexs.size() > 0;
	}
	public boolean hasOnlyOneUniqueIndex() {
	    return this.uniqueIndexs.size() == 1;
	}
	public boolean hasColumnNotInUniqueIndex() {
	    return uniqueIndexs.isEmpty() ? true : (columns.size() > uniqueIndexs.get(0).columns.size());
	}
	public int batchLimit(int placeHolderCount) {
	    return Math.min(placeHolderCount > 0 ? maxPreparedStmtCount / placeHolderCount : 2048, 2048);
	}
}
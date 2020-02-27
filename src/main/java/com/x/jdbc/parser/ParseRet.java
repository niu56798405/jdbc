package com.x.jdbc.parser;

import java.util.ArrayList;
import java.util.List;

import com.x.jdbc.analyzer.FColumn;

public class ParseRet {
	
    public final String sql;
    public List<FColumn> getColumns = new ArrayList<>();
    public List<FColumn> setColumns = new ArrayList<>();
    public List<FColumn> whereColumns = new ArrayList<>();
    
	public ParseRet(String sql) {
		this.sql = sql;
	}
}
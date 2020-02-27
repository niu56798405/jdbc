package com.x.jdbc.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.x.jdbc.analyzer.FColumn;
import com.x.jdbc.analyzer.FTable;

/**
 * sql 解析
 * @author 
 */
public class SQLParser {
	
	String sql;
	FTable ftable;
	public SQLParser(FTable ftable, String sql) {
		this.ftable = ftable;
		this.sql = compact(sql);
	}
	
	private String compact(String sql) {
	    String tmp = replace(replace(sql.trim(), "\\s+", " "), "\\s*([(),><!=]+)\\s*", "$1");
		return tmp.endsWith(";") ? tmp : tmp + ";";
	}

	private String replace(String src, String reg, String replace) {
	    Pattern p = Pattern.compile(reg);
	    Matcher matcher = p.matcher(src);
	    if(matcher.find()) {
	        return matcher.replaceAll(replace);
	    }
	    return src;
	}
	
	public ParseRet parse() {
		String token = firstToken().toLowerCase();
		if(token.equals("insert") || token.equals("replace")) {
			return InsertParser.parse(this);
		} else if(token.equals("update") || token.equals("delete")) {
			return UpdateParser.parse(this);
		} else if(token.equals("select")) {
			handleAsterisk();
			return SelectParser.parse(this);
		}
		throw new IllegalArgumentException("parse sql wrong [" + sql + "]");
	}

	//替换x.*|*为已知字段
	private void handleAsterisk() {
	    Pattern pattern = Pattern.compile("([\\w\\.]*\\*)\\s*[,{FROM}]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
		if(matcher.find()) {
		    for (int i = 0; i < matcher.groupCount(); i++) {
		        String sub = matcher.group(i+1);
		        String prex = sub.substring(0, sub.indexOf(".")+1);
		        sql = sql.replace(sub, packFTableColumnNames(prex));
            }
		}
	}

	private String packFTableColumnNames(String prex) {
	    if(ftable.columns.isEmpty()) {
	        throw new IllegalArgumentException("None mapping fields in [" + ftable.jTable.clazz + "]:[" + ftable.tableName + "]");
	    }
	    
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (FColumn column : ftable.columns) {
			sb.append(isFirst ? "" : ",");
			if(prex.length() == 0) {
			    sb.append(column.dbColumn.columnNameForQuery());
			} else {
			    sb.append(prex).append(column.dbColumn.name);
			}
			isFirst = false;
		}
		return sb.toString();
	}

	boolean hasNext() {
		return this.idx + 1 < sql.length();
	}
	
	String firstToken() {
		return nextToken();
	}
	
	String token;//token;
	private int idx;//current index
	char cc;//current char
	
	char nextChar() {
	    cc = sql.charAt(idx++);
	    return cc;
	}
	
	String nextToken() {
	    token = "";
	    
	    do {
	        nextChar();
	    } while(isWhitespace(cc));
		
		while(idx < sql.length() && !isDelimiter(cc)) {
		    token += cc;
		    nextChar();
		}
		return token;
	}
	
	boolean isWhitespace(char c) {
	    return c == ' ' || c == '\t';
	}
	
	boolean isDelimiter(char c) {
		return c == ' ' || c == ',' || c == '(' || c == ')' || c == ';';
	}
	
	static FColumn byDBColumnName(FTable ftable, String name) {
		FColumn fColumn = ftable.columnMap.get(name.toLowerCase());
		if(fColumn != null) {
			return fColumn;
		}
		throw new IllegalArgumentException("nonmapping java field for column : " + name);
	}
	
	//find [><!=]?
	static boolean hasQM(String token) {
	    Pattern pattern = Pattern.compile("[><!=]+\\?");
	    Matcher matcher = pattern.matcher(token);
	    return matcher.find();
	}
	
	//remove [><!=]?
	static String rmQm(String token) {
	    Pattern pattern = Pattern.compile("[><!=]+\\?");
        Matcher matcher = pattern.matcher(token);
        if(matcher.find()) {
            return token.substring(0, token.indexOf(matcher.group()));
        }
	    return token;
	}
	
	static boolean isWhere(String token) {
	    return "where".equals(token.toLowerCase());
	}
	
	static String parseColumnName(String tableName, String columnName) {
		String ret = columnName.substring(columnName.indexOf(".")+1);
		if(ret.startsWith("`")) {
		    ret = ret.substring(1, ret.length() - 1);
		}
		return ret;
	}
	
}

package com.x.jdbc.parser;

import com.x.jdbc.analyzer.FColumn;

//update table set xxx=?,yyy=? where zzz=?;
//delete from table where xxx=?;
//只解析 xxx=?
class UpdateParser {
	public static ParseRet parse(SQLParser parser) {
		ParseRet pr = new ParseRet(parser.sql);
		boolean wherPart = false;
		//解析所有 xxx=?
		while(parser.hasNext()) {
			String token = parser.nextToken();
			if(SQLParser.isWhere(token)) wherPart = true;
			if(SQLParser.hasQM(token)) {
				FColumn column = SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, SQLParser.rmQm(token)));
				pr.setColumns.add(column);
				if(wherPart) pr.whereColumns.add(column);
			}
		}
		return pr;
	}
}
package com.x.jdbc.parser;

//insert/replace into table()values(?, ?, ?, ?, ?);
class InsertParser {
	public static ParseRet parse(SQLParser parser) {
		ParseRet pr = new ParseRet(parser.sql);
		do {
		    parser.nextToken();
		} while(parser.cc != '(');//skip tokens before '('
		
		//解析 (column,...) 中所有column
		do {
			String token = parser.nextToken();
			
			if(token.equals("?"))
				throw new IllegalArgumentException("You can use table.insert instend, None insert column in the sql : " + parser.sql);
			
			pr.setColumns.add(SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, token)));
		} while(parser.cc != ')');
		
		return pr;
	}
}
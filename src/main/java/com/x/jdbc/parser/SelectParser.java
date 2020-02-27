package com.x.jdbc.parser;

//解析columns
class SelectParser {
	public static ParseRet parse(SQLParser parser) {
		ParseRet pr = new ParseRet(parser.sql);
		//先分析select ... from
		while(parser.hasNext()) {
		    parser.nextToken();
		    
			if(parser.token.toUpperCase().equals("FROM")) {
			    break;
			}
			if(parser.cc == ',') {
			    pr.getColumns.add(SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, parser.token)));
			    continue;
			}
			
			String alias = "";
			do {
			    alias += parser.token;
                alias += parser.cc;
                parser.nextToken();
			} while(parser.cc != ',' && !parser.token.toUpperCase().equals("AS") && !parser.token.toUpperCase().equals("FROM"));
			
			if(parser.cc == ',') {
			    pr.getColumns.add(SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, alias)));
			    continue;
			}
			
			if(parser.token.toUpperCase().equals("FROM")) {
			    pr.getColumns.add(SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, alias.trim())));//remove the space behind from
			    break;
			}
			
			if(parser.token.toUpperCase().equals("AS")) {
			    alias = parser.nextToken();
			    pr.getColumns.add(SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, alias)));
			    continue;
			}
            //can`t be here (Parser failed)
		}
		
		//解析where part
		while(parser.hasNext()) {
		    String token = parser.nextToken();
		    if(SQLParser.hasQM(token)) {
		    	pr.whereColumns.add(SQLParser.byDBColumnName(parser.ftable, SQLParser.parseColumnName(parser.ftable.tableName, SQLParser.rmQm(token))));
		    }
		}
		return pr;
	}
}
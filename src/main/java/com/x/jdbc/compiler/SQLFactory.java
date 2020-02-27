package com.x.jdbc.compiler;

import com.x.jdbc.Configuration;
import com.x.jdbc.Configuration.SQLType;
import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.RSParser;
import com.x.jdbc.TypePSSetter;
import com.x.jdbc.sequal.AbtSQL;
import com.x.jdbc.sequal.AsyncSQL;
import com.x.jdbc.sequal.ScheduledSQL;
import com.x.jdbc.sequal.SyncSQL;

public class SQLFactory<T> {
	
	private final int asyncModel;
	private final JDBCTemplate jdbcTemplate;
	
	public SQLFactory(int asyncModel, JDBCTemplate jdbcTemplate) {
		this.asyncModel = asyncModel;
		this.jdbcTemplate = jdbcTemplate;
	}

	public AbtSQL<T> newSQL(String sql, int condCout, int batchLimit, TypePSSetter<T> setter, RSParser<T> parser) {
		if(Configuration.getSqlType() == SQLType.SCHEDULED){
			return new ScheduledSQL<>(sql, jdbcTemplate, condCout, batchLimit, setter, parser);  
		}
		
		if(asyncModel == 1 || (asyncModel == -1 && Configuration.getSqlType() == SQLType.ASYNC)) {
			return new AsyncSQL<T>(sql, jdbcTemplate, condCout, batchLimit, setter, parser);
		}
		
		return new SyncSQL<T>(sql, jdbcTemplate, condCout, batchLimit, setter, parser);
	}
		
}

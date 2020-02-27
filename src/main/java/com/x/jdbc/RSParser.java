package com.x.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * ResultSet parser
 * @author 
 * @param <T>
 */
public interface RSParser<T> {
	
	T parse(ResultSet rs) throws SQLException;
	
}

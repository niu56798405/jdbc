package com.x.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PreparedStatement setter with obj
 * @author 
 */
public interface TypePSSetter<T> {

	void set(PreparedStatement pstmt, T obj) throws SQLException;
	
}

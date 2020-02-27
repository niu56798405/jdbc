package com.x.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PreparedStatement setter
 * @author 
 */
public interface PSSetter {
	
	void set(PreparedStatement pstmt) throws SQLException;
	
	PSSetter NONE = pstmt->{};
}

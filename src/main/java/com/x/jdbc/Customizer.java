package com.x.jdbc;

import com.x.jdbc.sequal.Sequal;

public class Customizer {
    
    public static Sequal queryWith(String... whereColumns) {
    	return Sequal.build().where(whereColumns).select();
    }
    
    public static Sequal deleteWith(String... whereColumns) {
        return Sequal.build().where(whereColumns).delete();
    }
    
    public static Sequal updateWith(String... whereColumns) {
        return Sequal.build().where(whereColumns).update();
    }
    
}

package com.x.jdbc.sequal;

import java.util.Collection;

import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.PSSetter;
import com.x.jdbc.RSParser;
import com.x.jdbc.TypePSSetter;

public class SyncSQL<T> extends AbtSQL<T> {
    
    public SyncSQL(String sql, JDBCTemplate jdbcTemplate, int condCout, int batchLimit, TypePSSetter<T> setter, RSParser<T> parser) {
        super(sql, jdbcTemplate, condCout, batchLimit, setter, parser);
    }
    //同步的update 方法
    //update(also insert,delete)
    public boolean update(T value) {
       return UpdateUtil.update(jdbcTemplate, sql, setter, value);
    }
        
    public boolean update(PSSetter setter) {
        return jdbcTemplate.update(sql, setter);
    }
    
    
    public boolean updateBatch(T[] values) {
        return UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setter, values);
    }
    public boolean updateBatch(Collection<T> values) {
        return UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setter, values);
    }
    
    
    /*-------------不推荐使用-------------*/
    public boolean updateBatchWithSetter(PSSetter... setters) {
        return UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setters);
    }
    public boolean updateBatchWithSetter(Collection<PSSetter> setters) {
        return UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setters);
    }
    
    public boolean updateByKey(Object key) {
        if(!isPureCond) {
            throw new IllegalArgumentException("None pure condition for [" + sql + "] to use SQL.updateByOneCond");
        }
        return jdbcTemplate.updateByOneCond(sql, key);
    }
    
}

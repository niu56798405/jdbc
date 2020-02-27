package com.x.jdbc.sequal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.PSSetter;
import com.x.jdbc.TypePSSetter;
import com.x.jdbc.Updatable;

public class UpdateUtil {
    
    public static <T> boolean updatable(T value) {
        if(value instanceof Updatable) {
            Updatable val = (Updatable) value;
            return val.updatable();
        }
        return true;
    }
    
    public static <T> boolean update(JDBCTemplate jdbcTemplate, String sql, TypePSSetter<T> setter, T value) {
        if(value instanceof Updatable) {
            Updatable val = (Updatable) value;
            if(val.updatable()) {
                int option = val.update();
                int ret = jdbcTemplate.updateAndReturnCause(sql, setter, value);
                if(ret == 0) {
                    val.commit(option);
                    return true;
                } else {
                    val.cancel(option, ret);
                }
            }
            return false;
        } else {
            return jdbcTemplate.update(sql, setter, value);
        }
    }
        
    private static <T> boolean updateBatch0(JDBCTemplate jdbcTemplate, int batchLimit, String sql, TypePSSetter<T> setter, List<T> vals) {
        int size = vals.size();
        int start = 0;
        while(size > 0 && start < size) {
            int end = Math.min(size, start+batchLimit);
            jdbcTemplate.updateBatch(sql, setter, vals.subList(start, end));
            start = end;
        }
        return true;
    }
    
    public static <T> boolean updateBatch(JDBCTemplate jdbcTemplate, int batchLimit, String sql, TypePSSetter<T> setter, Collection<T> values) {
        return updateBatch0(jdbcTemplate, batchLimit, sql, setter, filterAndCommit(values));
    }
    
    private static <T> List<T> filterAndCommit(Collection<T> values) {
        List<T> ret = new ArrayList<>(values.size());
        for (T t : values) {
            if(t instanceof Updatable) {
                Updatable val = (Updatable) t;
                if(!val.updatable()) continue;
                val.commit(val.update());
            }
            ret.add(t);
        }
        return ret;
    }

    public static <T> boolean updateBatch(JDBCTemplate jdbcTemplate, int batchLimit, String sql, TypePSSetter<T> setter, T[] values) {
        return updateBatch0(jdbcTemplate, batchLimit, sql, setter, filterAndCommit(values));
    }
    
    private static <T> List<T> filterAndCommit(T[] values) {
    	List<T> ret = new ArrayList<>(values.length);
        for (T t : values) {
            if(t instanceof Updatable) {
                Updatable val = (Updatable) t;
                if(!val.updatable()) continue;
                val.commit(val.update());
            }
            ret.add(t);
        }
        return ret;
    }
    
    
    private static <T> boolean updateBatch0(JDBCTemplate jdbcTemplate, int batchLimit, String sql, List<PSSetter> setters) {
        int size = setters.size();
        int start = 0;
        while(size > 0 && start < size) {
            int end = Math.min(size, start+batchLimit);
            jdbcTemplate.updateBatch(sql, setters.subList(start, end));
            start = end;
        }
        return true;
    }
    
    public static boolean updateBatch(JDBCTemplate jdbcTemplate, int batchLimit, String sql, Collection<PSSetter> setters) {
    	return setters.size() > batchLimit ? 
    			updateBatch0(jdbcTemplate, batchLimit, sql, new ArrayList<>(setters)) :
    			jdbcTemplate.updateBatch(sql, setters);
    }

    public static <T> boolean updateBatch(JDBCTemplate jdbcTemplate, int batchLimit, String sql, PSSetter[] setters) {
    	return (setters.length > batchLimit) ?
    			updateBatch0(jdbcTemplate, batchLimit, sql, Arrays.asList(setters)) :
    			jdbcTemplate.updateBatch(sql, setters);
    }
}

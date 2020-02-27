package com.x.jdbc.sequal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.PSSetter;
import com.x.jdbc.RSParser;
import com.x.jdbc.SQL;
import com.x.jdbc.TypePSSetter;

public abstract class AbtSQL<T> implements SQL<T> {

    protected final String sql;
    protected final boolean isPureCond;
    protected final JDBCTemplate jdbcTemplate;
    
    protected final TypePSSetter<T> setter;
    protected final RSParser<T> parser;
    
    protected final int batchLimit;

    public AbtSQL(String sql, JDBCTemplate jdbcTemplate, int condCout, int batchLimit, TypePSSetter<T> setter, RSParser<T> parser) {
        this.sql = sql;
        this.jdbcTemplate = jdbcTemplate;
        this.isPureCond = (condCout == 1);//是否where子句中只有一个参数 非单一参数不支持byOneCond方法
        this.batchLimit = batchLimit;
        this.setter = setter;
        this.parser = parser;
    }
    
    public TypePSSetter<T> getSetter() {
		return setter;
	}

	public RSParser<T> getParser() {
		return parser;
	}
	
	public String getSql() {
		return sql;
	}
	
    //fetch
    public T fetchOne(PSSetter setter) {
        return jdbcTemplate.fetchOne(sql, setter, parser);
    }
    public List<T> fetchMany(PSSetter setter) {
        return (List<T>) jdbcTemplate.fetchMany(sql, setter, parser);
    }
    public List<T> fetchMany(Map<String, Object> param) {
    	StringBuilder builder = new StringBuilder(sql);
    	builder.append(" WHERE ");
    	Iterator<String> iterator = param.keySet().iterator();
    	while (iterator.hasNext()) {
			String key = iterator.next();
	    	builder.append(key);
	    	builder.append(" = '");
	    	builder.append(param.get(key));
	    	builder.append("'");
	    	if (iterator.hasNext()) {
		    	builder.append(" AND ");
			}			
		}    	
        return (List<T>) jdbcTemplate.fetchMany(builder.toString(), null, parser);

    }
    
    public List<T> fetchMany(String key, Object value) {
    	StringBuilder builder = new StringBuilder(sql);
    	builder.append(" WHERE ");
    	builder.append(key);
    	builder.append(" = '");
    	builder.append(value);
    	builder.append("'");						    	
        return (List<T>) jdbcTemplate.fetchMany(builder.toString(), null, parser);
    }
     
    public List<T> fetchManyByIndex(Object idx) {
        return (List<T>) jdbcTemplate.fetchManyByOneCond(sql, idx, parser);
    }

    public T fetchOneByKey(Object key) {
        checkPureCond();
        return jdbcTemplate.fetchOneByOneCond(sql, key, parser);
    }
    
    protected void checkPureCond() {
        if(!isPureCond) {
            throw new IllegalArgumentException("None pure condition for [" + sql + "] to use byKey methods");
        }
    }
    
	@Override
    public long insertAndGenKey(T value) {
        return jdbcTemplate.fetchIncrement(sql, setter, value);
    }

    @Override
    public String toString() {
        return sql;
    }
    
}
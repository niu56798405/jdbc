package com.x.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 对应一个SQL
 * @author 
 * @param <T>
 */
public interface SQL<T> {
	
	//fetch
	public T fetchOne(PSSetter setter);
	
	public List<T> fetchMany(PSSetter setter);
	
	public List<T> fetchMany(String fieldName, Object value);
	
	public List<T> fetchMany(Map<String, Object> param);

	public List<T> fetchManyByIndex(Object idx);
	
	//update(also insert,delete)
	public boolean update(T value);
	public boolean update(PSSetter setter);
	
	public boolean updateBatch(T[] values);
	public boolean updateBatch(Collection<T> values);
	
	/*-------------不推荐使用-------------*/
//	public boolean updateBatchWithSetter(PSSetter... setters);
//	public boolean updateBatchWithSetter(Collection<PSSetter> setters);
	
	public boolean updateByKey(Object key);
	public T fetchOneByKey(Object key);
	
	public long insertAndGenKey(T value);
}

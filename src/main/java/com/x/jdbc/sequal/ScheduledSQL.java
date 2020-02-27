package com.x.jdbc.sequal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.PSSetter;
import com.x.jdbc.RSParser;
import com.x.jdbc.TypePSSetter;
/**
 * 定时对 数据进行持久化
 * 
 * @author niu
 *
 * @param <T>
 */
public class ScheduledSQL<T> extends AbtSQL<T> {

	private LinkedBlockingQueue<T> datas = new LinkedBlockingQueue<>();

	private LinkedBlockingQueue<PSSetter> setterList = new LinkedBlockingQueue<>();
		

    public ScheduledSQL(String sql, JDBCTemplate jdbcTemplate, int condCout, int batchLimit, TypePSSetter<T> setter, RSParser<T> parser) {
        super(sql, jdbcTemplate, condCout, batchLimit, setter, parser);
    }

   
    public boolean update(T value){
    	return datas.add(value);
    }
    
    /**
     * 不建议 使用此方法
     */
    public boolean update(PSSetter setter) {
        return setterList.add(setter);
    }
    
    public void persistence(){    		
		long start = System.currentTimeMillis();     	   	    	
		dataPersistence();
		setterPersistence(); 
		long end = System.currentTimeMillis();
		System.out.println("time use:" + (end - start));
    }
    
    private void dataPersistence(){
		if(!datas.isEmpty()){
			List<T> list = new ArrayList<>();
			datas.drainTo(list);
			System.out.println("size:" + list.size());
			Iterator<T> iterator = list.iterator();
			List<T> temp = new ArrayList<>();
    		while (iterator.hasNext()) {
    			T t = iterator.next();    		
    			temp.add(t);
				if(temp.size() >= batchLimit || !iterator.hasNext()){					
		            jdbcTemplate.updateBatch(sql, setter, temp);					
		            temp.clear();
				}
			}
    	}
    }
    
    private void setterPersistence(){
     	if(!setterList.isEmpty()){
    		List<PSSetter> setterTemp = new LinkedList<>(); 
    		setterList.drainTo(setterTemp);
    		UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setterTemp);
    	} 
    }
        
    public boolean updateBatch(T[] values) {
    	for(T t : values){
    		update(t);
    	}
        return true;
    }
    
    public boolean updateBatch(Collection<T> values) {
     	for(T t : values){
    		update(t);
    	}
    	return true;       
    }
    
    
    public boolean updateByKey(Object key) {
        if(!isPureCond) {
            throw new IllegalArgumentException("None pure condition for [" + sql + "] to use SQL.updateByOneCond");
        }
        return jdbcTemplate.updateByOneCond(sql, key);
    }
    
}

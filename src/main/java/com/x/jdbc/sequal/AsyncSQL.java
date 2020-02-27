package com.x.jdbc.sequal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.x.jdbc.Configuration;
import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.PSSetter;
import com.x.jdbc.RSParser;
import com.x.jdbc.TypePSSetter;

/**
 * 查询还是同步
 * 异步保存
 * @author 
 * @param <T>
 */
public class AsyncSQL<T> extends AbtSQL<T> {
    
    final SQLExecutor exec;
    
    public AsyncSQL(String sql, JDBCTemplate jdbcTemplate, int condCout, int batchLimit, TypePSSetter<T> setter, RSParser<T> parser) {
        super(sql, jdbcTemplate, condCout, batchLimit, setter, parser);
        this.exec = Configuration.getExecutor();
    }
    
    //update(also insert, delete)
    public boolean update(final T value) {
        if(UpdateUtil.updatable(value)) {
            execute(new SingleValUpdateTask<>(this, value));
            return true;
        }
        return false;
    }
    public boolean update(final PSSetter setter) {
        execute(()->jdbcTemplate.update(sql, setter));
        return true;
    }
    
    
    public boolean updateBatch(final T[] values) {
        execute(()->UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setter, values));
        return true;
    }
    public boolean updateBatch(final Collection<T> values) {
        List<T> vs = new ArrayList<>(values);
        execute(()->UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setter, vs));
        return true;
    }
    
    /*-------------不推荐使用-------------*/
    public boolean updateBatchWithSetter(final PSSetter... setters) {
        execute(()->UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setters));
        return true;
    }
    public boolean updateBatchWithSetter(final Collection<PSSetter> setters) {
        execute(()->UpdateUtil.updateBatch(jdbcTemplate, batchLimit, sql, setters));
        return true;
    }
    
    public boolean updateByKey(final Object key) {
        checkPureCond();
        execute(()->jdbcTemplate.updateByOneCond(sql, key));
        return true;
    }
    
    private void execute(SQLTask task) {
        if(exec.isShutdown()) {
            task.run();
            return;
        }
        try {
            exec.execute(task);
        } catch (Throwable e) {
            task.run();
        }
    }
    
    static final class SingleValUpdateTask<T> implements SQLTask {
        final AbtSQL<T> sql;
        final T value;
        public SingleValUpdateTask(AbtSQL<T> sql, T value) {
            this.sql = sql;
            this.value = value;
        }
        public void exec() {
            UpdateUtil.update(sql.jdbcTemplate, sql.sql, sql.setter, value);
        }
        @Override
        public final int hashCode() {
            return System.identityHashCode(value);
        }
    }
    
}

package com.x.jdbc;


/**
 * 
 * 提供给SQL.Update时判断更新状态
 * 批量更新无效
 * @author 
 *
 * @param <T> attachObj 多线程时可以利用
 */
public interface Updatable {
    
    public static final int DUPLICATE_KEY_ERROR = 1062;
    
    boolean updatable();
    
    /**
     * 开始更新
     * @return option
     */
    int update();
    
    /**
     * 更新成功
     * @param option
     */
    void commit(int option);
    
    /**
     * 更新失败
     * @param option
     */
    void cancel(int option, int cause);
    
}

package com.x.jdbc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.sql.DataSource;

import com.x.jdbc.codec.ArrayCodec;
import com.x.jdbc.codec.Codec;
import com.x.jdbc.codec.CodecUtil;
import com.x.jdbc.codec.FieldCodec;
import com.x.jdbc.datasource.DBConf;
import com.x.jdbc.datasource.JdbcPool;
import com.x.jdbc.sequal.SQLExecutor;


/**
 * type query && jdbc configuration
 * @author 
 */
public class Configuration {
    
	private static SQLType sqlType = SQLType.SYNC;
    
    private static int dbThreads;
    
    private static SQLExecutor executor;
    
    private static Map<FieldType, CodecGetter> fieldCodecs = new HashMap<>();
    
    private static Mapper mapper = c -> c;//default do nothing
    
    private static Map<Enum<?>, JDBCTemplate> jdbcTemplates = new HashMap<>();
    
    private static int defaultDelimiters = Codec.DEFAULT_DELIMITERS;
    
    private static boolean forceUseIndex = false;//定制的sql各种条件必须使用Index
    
    private static boolean instupPrior = false;//默认优先使用insert update 代替insert
    
    private static ScheduledExecutorService timer;
    
    private Configuration() {
    }
    
    static {
        //default global FieldCodec
        configureFieldCodec(c->c.isEnum()||ArrayCodec.isArray(c)||List.class.isAssignableFrom(c)||Set.class.isAssignableFrom(c), f->CodecUtil.fetchCodec(f));
    }
    
    //async moduel
    public static void configureAsyncModel(int nThreads) {
        dbThreads = nThreads;
    }
    public static SQLType getSqlType() {
        return sqlType;
    }
    public synchronized static SQLExecutor getExecutor() {
        if(executor == null && sqlType == SQLType.ASYNC)
            executor = new SQLExecutor(dbThreads);
        return executor;
    }
    public static void shutdown() {
        if(executor != null) executor.shutdown();
    }
    
    //global mapper
    public static void configureMapper(Mapper mapper) {
        Configuration.mapper = mapper;
    }
    public static String map(String columnName) {
        return mapper.map(columnName);
    }
    
    //global field codec
    public static void configureFieldCodec(Class<?> clazz, FieldCodec<?, ?> codec) {
        configureFieldCodec(clazz::equals, codec);
    }
    public static void configureFieldCodec(FieldType type, FieldCodec<?, ?> codec) {
        configureFieldCodec(type, (f)->codec);
    }
    public static void configureFieldCodec(FieldType type, CodecGetter getter) {
        fieldCodecs.put(type, getter);
    }
    public static boolean hasFieldCodec(Class<?> fieldType) {
        return fieldCodecs.entrySet().stream().filter(e->e.getKey().is(fieldType)).map(e->e.getValue()).findAny().isPresent();
    }
    public static FieldCodec<?, ?> getFieldCodec(Field field) {
        return fieldCodecs.entrySet().stream().filter(e->e.getKey().is(field.getType())).map(e->e.getValue()).findAny().orElse(f->null).get(field);
    }
    //array/list/set split default elimiters
    /**@see Codec.DEFAULT_DELIMITERS*/
    public static void configureDefaultDelimiters(int delimiters) {
        defaultDelimiters = delimiters;
    }
    public static int getDefaultDelimiters() {
        return defaultDelimiters;
    }
    
    /**所有sql必须在有索引的情况下进行*/
    public static void configureForceUseIndex(boolean forceUseIndex) {
        Configuration.forceUseIndex = forceUseIndex;
    }
    public static boolean isCustomForceUseIndex() {
        return forceUseIndex;
    }
    
    public static void configureInstupPrior(boolean instupPrior) {
        Configuration.instupPrior = instupPrior;
    }
    public static boolean isInstupPrior() {
        return instupPrior;
    }
    
    //database
    public static JDBCTemplate configureDatabase(Enum<?> type, DBConf conf) {
        return configureDatabase(type, JdbcPool.buildDataSource(conf));
    }
    public static JDBCTemplate configureDatabase(Enum<?> type, DataSource source) {
        JDBCTemplate jdbcTemplate = new JDBCTemplate(source);
        jdbcTemplates.put(type, jdbcTemplate);
        return jdbcTemplate;
    }
    public static JDBCTemplate getJDBCTemplate(Object type) {
        return jdbcTemplates.get(type);
    }
    
    @FunctionalInterface
    public interface FieldType {
        public boolean is(Class<?> clazz);
    }
    @FunctionalInterface
    public interface CodecGetter {
        public FieldCodec<?, ?> get(Field field);
    }
    
    @FunctionalInterface
    public interface Mapper {
        /**map column name to field name*/
        String map(String columnName);
    }
    
    public static ScheduledExecutorService getTimer(){
    	if(timer == null){
    		dbThreads = dbThreads <= 0 ? Runtime.getRuntime().availableProcessors() : dbThreads;
    		timer = new ScheduledThreadPoolExecutor(dbThreads); 
    	}
    	
    	return timer;	
    }
   
    
    public enum SQLType{
    	SYNC,//同步
    	ASYNC,//异步
    	SCHEDULED //定时批量写入
    }
}

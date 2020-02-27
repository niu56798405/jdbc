package com.x.jdbc.analyzer;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.x.jdbc.Configuration;
import com.x.jdbc.JDBCTemplate;
import com.x.jdbc.TypeParser;
import com.x.jdbc.codec.FieldCodec;
import com.x.jdbc.compiler.SQLFactory;
import com.x.jdbc.sequal.Sequal;

public class AnalyzerInfo<T> {
    
    private transient String tableName;
    private transient Class<T> clazz;
    private transient Map<String, String> mappings;
    private transient Sequal[] sequals = new Sequal[0];
    private transient Enum<?> dbType;
    private transient JDBCTemplate jdbcTemplate;
    private transient int asyncModel = -1;
    private transient Map<String, FieldCodec<?, ?>> fieldCodeces;
    private transient Map<Class<?>, FieldCodec<?, ?>> fieldTypeCodeces;
    private transient TypeParser<T> typeParser;
    private transient SQLFactory<T> factory;
    
    public AnalyzerInfo(Class<T> clazz) {
        this.clazz = clazz;
        this.mappings = new HashMap<String, String>();
        this.fieldCodeces = new HashMap<String, FieldCodec<?,?>>();
        this.fieldTypeCodeces = new HashMap<Class<?>, FieldCodec<?, ?>>();
    }
    
    public void configureAsyncModel(boolean asyncModel) {
        this.asyncModel = (asyncModel ? 1 : 0);
    }

    public void binding(Enum<?> type) {
        this.dbType = type;
    }
    
    public void binding(Enum<?> type, String tableName){
        this.dbType = type;
        this.tableName = tableName;
    }
    
    public void binding(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public void binding(JDBCTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }
    
    public void binding(int idx, Sequal sequal) {
        int nl = Math.max(idx, sequals.length) + 1;
        sequals = Arrays.copyOf(sequals, nl);
        sequals[idx] = sequal;
    }
    
    public void binding(String fieldName, FieldCodec<?, ?> codec) {
        this.fieldCodeces.put(fieldName, codec);
    }
    
    public void binding(Class<?> fieldType, FieldCodec<?, ?> codec) {
        this.fieldTypeCodeces.put(fieldType, codec);
    }
    
    public void binding(TypeParser<T> typeParser) {
        this.typeParser = typeParser;
    }
    
    public boolean hasCodec(String fieldName) {
        return fieldCodeces.containsKey(fieldName);
    }
    
    public boolean hasCodec(Class<?> fieldType) {
        return fieldTypeCodeces.containsKey(fieldType) || Configuration.hasFieldCodec(fieldType);
    }
    
    public FieldCodec<?, ?> getCodec(String fieldName) {
    	return fieldCodeces.get(fieldName);
    }
    public FieldCodec<?, ?> getCodec(Field field) {
        return fieldTypeCodeces.containsKey(field.getType()) ? fieldTypeCodeces.get(field.getType()) : Configuration.getFieldCodec(field);
    }
    
    public void mapping(String fieldName, String columnName) {
        this.mappings.put(fieldName, columnName);
    }
    
    public Map<String, String> mappings() {
        return mappings;
    }
    
    public TypeParser<T> typeParser() {
        return this.typeParser;
    }
    
    public Sequal[] sequals() {
        return this.sequals;
    }
    
    public JDBCTemplate jdbcTemplate() {
        if(jdbcTemplate == null)
            jdbcTemplate = Configuration.getJDBCTemplate(dbType);
        return jdbcTemplate;
    }
    
    public boolean hasBindingTable() {
        return this.tableName != null;
    }
    
    public Class<T> clazz() {
        return this.clazz;
    }
    
    public Map<String, FieldCodec<?, ?>> fieldCodeces() {
        return this.fieldCodeces;
    }
    
    public synchronized SQLFactory<T> buildFactory() {
        if(factory == null) {
            factory = new SQLFactory<>(asyncModel, this.jdbcTemplate());
        }
        return factory;
    }
    
    public String tableName() {
        return tableName;
    }
    
    public Map<Class<?>, FieldCodec<?, ?>> typeCodeces() {
        return fieldTypeCodeces;
    }

}

package com.x.jdbc.codec;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonCodec<T> implements FieldCodec<T, String>{
	private Class<T> clazz;
	private Type genericType;
		

	private JsonCodec(Class<T> clazz, Type genericType) {
		super();
		this.clazz = clazz;
		this.genericType = genericType;
	}
	
	private JsonCodec(Class<T> clazz) {
		super();
		this.clazz = clazz;		
	}

	
	

	@Override
	public String encode(T fieldValue) {
		return JSON.toJSONString(fieldValue);
	}

	@Override
	public T decode(String columnValue) {
		if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			return JSONObject.parseObject(columnValue, genericType);
		} 
		
		return JSONObject.parseObject(columnValue, clazz);
	}

	public static <T> JsonCodec<T> buildCodec(Class<T> clazz, Type genericType) throws Exception{
		
		if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			if (genericType == null) {
				throw new Exception(" 需指定  泛型 type ");
			}else {
				return new JsonCodec<>(clazz, genericType);
			}
		}		
		return new JsonCodec<>(clazz);
	}
	
		
	public static <T> JsonCodec<T> buildCodec(Class<T> clazz) throws Exception{	
		if (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			throw new Exception(" 需指定  泛型 type ");
		}
		return new JsonCodec<>(clazz);					
	}
	
	
}

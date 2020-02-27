package com.x.jdbc.analyzer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.x.jdbc.TypeParser;
import com.x.jdbc.codec.FieldCodec;

public class FCodecs {
	
	private Map<String, FieldCodec<?, ?>> codecs = new HashMap<>();
	private TypeParser<?> typeParser;
	
	public TypeParser<?> getTypeParser() {
        return typeParser;
    }

    public void setTypeParser(TypeParser<?> typeParser) {
        this.typeParser = typeParser;
    }
    
    public boolean hasTypeParser() {
        return this.typeParser != null;
    }
    
    public void put(String fieldName, FieldCodec<?, ?> codec) {
		codecs.put(fieldName, codec);
	}
	
	public boolean hasCodec(String fieldName) {
		return codecs.containsKey(fieldName);
	}

	public Class<?> findColumnTypeFromCodec(String fieldName) {
        FieldCodec<?, ?> codec = codecs.get(fieldName);
        Class<?> clazz = codec.getClass();
        do {
            Type[] genericInterfaces = clazz.getGenericInterfaces();
            for (Type type : genericInterfaces) {
                if(type instanceof ParameterizedType) {
                    ParameterizedType ptype = (ParameterizedType) type;
                    if(ptype.getRawType().equals(FieldCodec.class)) {
                        return (Class<?>) ptype.getActualTypeArguments()[1];
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while((!Object.class.equals(clazz)));
        
        return null;//can`t be here
    }

	public FieldCodec<?, ?> get(String key) {
	    return codecs.get(key);
	}
	
	public Map<String, FieldCodec<?, ?>> get() {
		return codecs;
	}
	
}

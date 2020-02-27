package com.x.jdbc.codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 提供给字段进行的注释 方便类型转换
 * Enum <--> int [value为int的值]
 * 分隔符用char表示, 以下char char1 char2均表示分隔符
 * Array|List|Set <--> String [value为分隔符, value算法: char1 | (char2 << 8)]
 *  Demo: @Codec('-' | ('~' << 8))
 *        @Codec('-')
 *        @Codec('~' << 8)
 * @author 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Codec {
	
	public int value();
    
	/**
	 * 分隔符类型 value算法 char1 + (char2 << 8)
	 * 暂时只技能int, long, String
	 */
	public static final int DEFAULT_DELIMITERS = ',' | ('|' << 8);
	
}
package com.x.jdbc.test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class CodecTest {
	private List<Integer> list;
	@Test
	public void codec(){
		List<Integer> list = new LinkedList<>();
		long s1 = System.currentTimeMillis();
		for (int i = 0; i < 5000; i++) {
			list.add(i);
		}
		System.out.println(System.currentTimeMillis() - s1);
		
		s1 = System.currentTimeMillis();
		
		for (int i = 0; i < 1000; i++) {
			String lencode = lencode(list);
			ldecode(lencode);
		}		
		Logger.info("time 1:" + (System.currentTimeMillis() - s1));

		
	}
	
	private static String lencode(List<Integer> list) {
		StringBuilder builder = new StringBuilder();
		list.forEach(v -> {
			builder.append(v);
			builder.append(',');
		});
		return builder.toString();
	}
	
	private static List<Integer> ldecode(String s) {
		String[] split = s.split(",");
		return Arrays.stream(split).map(Integer::parseInt).collect(Collectors.toList());		
	}
	
	@Test
	public void jsonCodec() throws Exception{
		List<Integer> list = new LinkedList<>();
		for (int i = 0; i < 100; i++) {
			list.add(i);
		}
		Type genericType = CodecTest.class.getDeclaredField("list").getGenericType();
		
		long s1 = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			String jsonString = JSON.toJSONString(list);
			JSONObject.parseObject(jsonString, genericType);
		}
		Logger.info("time 2:" + (System.currentTimeMillis() - s1));
		
	}
	
}

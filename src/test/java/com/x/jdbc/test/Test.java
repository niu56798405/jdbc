package com.x.jdbc.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;
import com.x.jdbc.codec.Json;

public class Test {
	private long id;
	private Long tid;
	private String name;
	private XList<Integer> cardIds = new XList<>();
	private List<Long> longList;
	private Map<Integer, String> map;


	
	public Test(long id, String name, List<Long> longList) {
		super();
		this.id = id;
		this.longList = longList;
		this.name = name;
		
	}
	
	
	public Map<Integer, String> getMap() {
		return map;
	}

	

	public void setMap(Map<Integer, String> map) {
		this.map = map;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Test() {
		this.cardIds = new XList<>();
		this.longList = new LinkedList<>();
	}


	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public List<Integer> getCardIds() {
		return cardIds;
	}
	public void setCardIds(XList<Integer> cardIds) {
		this.cardIds = cardIds;
	}
	public List<Long> getLongList() {
		return longList;
	}
	public void setLongList(List<Long> longList) {
		this.longList = longList;
	}


	public Long getTid() {
		return tid;
	}


	public void setTid(Long tid) {
		this.tid = tid;
	}


	@Override
	public String toString() {
		return "Test [id=" + id + ", name=" + name + ", cardIds=" + cardIds + ", longList=" + longList + ", map=" + ""
				+ "]";
	}
	

}

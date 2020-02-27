package com.x.jdbc.test;



public class TA {

	private int id;
	public String name;
	public T enumT;
	int[][] arrayT;
//	public List<Integer> arrayT;
	
	private TA() {
	}
	
	public TA(int id, String name) {
		this.id = id;
		this.name = name;
		enumT = T.b;
		arrayT = new int[][]{{1,2},{3,4}};
//		arrayT = Arrays.asList(1, 2);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "TA [id=" + id + ", name=" + name + "]";
	}
	

	
}

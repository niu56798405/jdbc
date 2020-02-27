package com.x.jdbc.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.x.jdbc.Configuration;
import com.x.jdbc.Customizer;
import com.x.jdbc.PSSetter;
import com.x.jdbc.TableQuery;
import com.x.jdbc.TypeQuery;
import com.x.jdbc.codec.JsonCodec;
import com.x.jdbc.datasource.DBConf;
import com.x.jdbc.sequal.Sequal;

public class JDBCTest {
	
	public static enum TE {
		NONE;
	}

	public static void main(String[] args) throws Exception {
		
	    DBConf conf = new DBConf("localhost", 3306, "db_service_001", "root", "root"); 
	    
	    Configuration.configureDatabase(TE.NONE, conf);
	    TableQuery<Test> q = new TableQuery<Test>(Test.class).binding(TE.NONE, "test").binding(0, Customizer.queryWith("name"));
	
	    
	    TableQuery<Test> q1 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test1").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q2 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test2").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q3 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test3").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q4 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test4").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q5 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test5").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q6 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test6").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q7 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test7").binding(0, Customizer.queryWith("name"));
//	    TableQuery<Test> q8 = new TableQuery<Test>(Test.class).binding(TE.NONE, "test8").binding(0, Customizer.queryWith("name"));
	    List<Integer> intList = new LinkedList<>();
	    intList.add(1);
	    intList.add(2);
	    intList.add(3);
	    intList.add(4);
	    List<Long> longList = new LinkedList<>();
	    longList.add(9L);
	    longList.add(10L);
	    longList.add(11L);
	    for(int i = 1;i<200;i++){
	    	longList.add(56968L);
	    }
	    long start = System.currentTimeMillis();
//	    List<Test> aaaa = q.fetchMany("name", "3356874");

	    
//	    List<Test> aaaa1 = q.fetchMany("id", 222);
	    Test test1  = new Test(1, "3356874", longList);
	    
	    List<Test> tests = new LinkedList<>();
		for(int i = 1; i<=10000; i++){
			tests.add(new Test(i, "3356874", longList));
			if (tests.size() >= 1000) {
				start = System.currentTimeMillis();
				q.instupBatch(tests);
				long end = System.currentTimeMillis();
				System.out.println("batch insert time use:"+(end - start));
				tests.clear();
			}
		}
		
	    
	    
	    
	
//	    q.instup(test1);
	    Test fetchoneByKey3 = q.fetchoneByKey(2);
		Test fetchoneByKey2 = fetchoneByKey3;
		Test fetchoneByKey = fetchoneByKey2;
	    System.out.println(fetchoneByKey);
//	    List<Test> all = q.fetchall();
//	    List<Test> all1 = q1.fetchall();
//	    List<Test> all2 = q2.fetchall();
//	    List<Test> all3 = q3.fetchall();
	    
	    
//	    all.sort(Comparator.comparingInt(Test::getId));
//	    
//	    for (int i = 0; i < all.size(); i++) {
//	    	Test test = all.get(i);
//	    	if (test.getId() != i+1) {
//		    	System.out.println("id :"+ test.getId() +" index :" + i);
//			}
//		}
//	    
	    
	    long end = System.currentTimeMillis();
	    //System.out.println("time use:"+ (end - start) + " size " + all.size());

	    
	    long end2 = System.currentTimeMillis();
	    System.out.println("time use >>> :"+ (end2 - end));
	    
//	    for(int i = 1;i< 8000;i++){
//	    	long s1 = System.currentTimeMillis();
//	    	List<Test> fetchMany = q.fetchMany("name", "abc");
//		    System.out.println("get time use!!!! >>> :"+ (System.currentTimeMillis() - s1));
////
//	    } 
//	    System.out.println("get All time use!!!! >>> :"+ (System.currentTimeMillis() - end2));

	    long index = 1;
//	    while (true) {
	    long s1 = System.currentTimeMillis();
//		    for(int i = 100000;i< 1000000;i++){
//
//		    	Test test  = new Test(++index, String.valueOf(i), longList);
//	    	
//	    		q.insert(test);
////	    		q.fetchoneByKey(i);
////		    	System.out.println(test);
////		    	q1.instup(test);
////		    	q2.instup(test);
////		    	q3.instup(test);
////		    	q4.instup(test);
////		    	q5.instup(test);
////		    	q6.instup(test);
////		    	q7.instup(test);
////		    	q8.instup(test);
//		    }

		    System.out.println("insert time use!!!! >>> :"+ (System.currentTimeMillis() - s1));
		    
		    
		    s1 = System.currentTimeMillis();
		    for(int i = 1;i< 10000;i++){
		    		final String name = String.valueOf(i);
	    			q.load(0).fetchMany(s->s.setString(1, name));

		    }

		    System.out.println("select time use!!!! >>> :"+ (System.currentTimeMillis() - s1));
		    
		    
		    s1 = System.currentTimeMillis();
		    for(int i = 1;i< 10000;i++){
	    	
	    			q.fetchoneByKey(i);

		    }

		    System.out.println("get time use!!!! >>> :"+ (System.currentTimeMillis() - s1));
		    
 
//		    Thread.sleep(1000);

//		}
	    
	    
//	    Test test2  = new Test(2,"222",intList,longList);
//	    q.instup(test2);
//	    
//	    Test fetch1 = fetchoneByKey;
//	    Map<Integer, String> map = fetch1.getMap();
//	    if (map == null) {
//			map = new HashMap<>();
//			fetch1.setMap(map);
//		}
//	    map.put(1, "987");
//	    map.put(2, "678");
//	    q.instup(fetch1);
//	    
//	    Test fetch2 = q.fetchoneByKey(2);
//	    System.out.println(fetch2);
//
//	    List<Test> fetchMany = q.load(0).fetchMany(s -> s.setString(1, "111"));
//	    System.out.println(fetchMany.size());
//	    Test test = q.load(0).fetchOneByKey("111");
//	    
//	    System.out.println(test);
	    
//	    System.out.println(q.insertAndGenKey(new Test()));
	    
//		TypeQuery<TA> query = new TypeQuery<TA>(TA.class)
//				.binding(TE.NONE, "ta")
////				.binding(2, "select a.* from ta a where Ident = ?")			
//                .compile();
//		
//		TA ta = new TA(100, "nihao");
//		
//		System.out.println("insert ret : " + query.insert(ta));
//		System.out.println("queryone by key ret : " + query.fetchoneByKey(100));
//		
//		ta.name = "sayHey";
//		System.out.println("update by key ret : " + query.updateByKey(ta));
//		System.out.println("delete by key ret : " + query.deleteByKey(100));
////		System.out.println("select by sql : " + query.load(2).fetchOne(new PSSetter() {
////			@Override
////			public void set(PreparedStatement pstmt) throws SQLException {
////				pstmt.setLong(1, 1000);
////			}
////		}));
//		System.out.println("select by sql : " + query.load(3).fetchOne(pstmt->pstmt.setLong(1, 1000)));
//		ta.name ="google";
////		System.out.println("update by sql : " + query.load(1).update(ta));
//		System.out.println("queryall ret : " + query.fetchall());
//		ta.setId(1000);
//		System.out.println("delete by sql : " + query.load(0).update(ta));
//		System.out.println("insert by sql : " + query.load(4).update(ta));
//		System.out.println("queryall ret : " + query.fetchall());
	}

}

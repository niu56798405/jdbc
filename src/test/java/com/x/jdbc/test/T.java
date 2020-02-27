package com.x.jdbc.test;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.x.jdbc.codec.Codec;

public enum T {

    @Codec(1) a,
    @Codec(2) b;

    public static void main(String[] args) throws Exception {
		Constructor<TA> constructor = TA.class.getConstructor();
		constructor.setAccessible(true);
		TA newInstance = constructor.newInstance();
		System.out.println(newInstance);
    	
        String sql = "Select 1 a.*, * from ( table where ) 1";
        //select.*[\\w\\.]*\\*.*from
        Pattern pattern = Pattern.compile("([\\w\\.]*\\*)\\s*[,{FROM}]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if(matcher.find()) {
            int groupCount = matcher.groupCount();
            for (int i = 0; i < groupCount; i++) {
                String sub = matcher.group(i+1);
                sql = sql.replace(sub, "nihaoa");
            }
        }
        System.out.println(sql);
        System.out.println("a.*".substring(0, "*".indexOf(".")+1));
    }
    
}

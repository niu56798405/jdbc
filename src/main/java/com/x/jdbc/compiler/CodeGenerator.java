package com.x.jdbc.compiler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.x.jdbc.RSParser;
import com.x.jdbc.TypePSSetter;
import com.x.jdbc.TypeParser;
import com.x.jdbc.analyzer.FCodecs;
import com.x.jdbc.analyzer.FColumn;
import com.x.jdbc.analyzer.FTable;
import com.x.jdbc.codec.FieldCodec;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class CodeGenerator {
	
	private transient final static AtomicInteger ai = new AtomicInteger(0);
    private static int complieIndex() {
        return ai.incrementAndGet();
    }

    @SuppressWarnings("unchecked")
    static <T> RSParser<T> makeParser(FTable ftable, List<FColumn> columns) throws Exception {
    	if(columns.isEmpty()) return null;
    	
        String clazzName = ftable.clazz.getName();
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.makeClass(clazzName + "$RSParser_" + (complieIndex()));
        clazz.addInterface(pool.getCtClass(RSParser.class.getName()));
        
        StringBuilder body = new StringBuilder("{");
        if(ftable.codecs.hasTypeParser()) {
            body.append(clazzName).append(" _tmp_ = (").append(clazzName).append(")this.").append(typeParserFieldName()).append(".make(").append("$1").append(");")
                .append("if(_tmp_ == null) {")
                .append("_tmp_ = new ").append(clazzName).append("();")
                .append("}");
        } else {
            body.append(clazzName).append(" _tmp_ = new ").append(clazzName).append("();");
        }
        
        int index = 1;
        for (FColumn fColumn : columns) {
            body.append(TypeCodec.makeParserBodyElement(ftable.codecs, ftable.jTable, fColumn, "_tmp_", "$1", index));
            ++index;
        }
        
        if(ftable.codecs.hasTypeParser()) {
            body.append(typeParserFieldName()).append(".apply(").append("_tmp_").append(");");
        }
        body.append("return _tmp_;").append("}");
        
        makeCodecFieldsAndConstructor(ftable.codecs, pool, clazz);//先解析columns才有codecFields, 先添加codecFields才能生成body
        
        CtMethod parser = CtNewMethod.copy(pool.getMethod(RSParser.class.getName(), "parse"), clazz, null);
        parser.setBody(body.toString());
        clazz.addMethod(parser);
        
        return (RSParser<T>) newInstance(ftable.codecs, clazz);
    }

    @SuppressWarnings("unchecked")
    static <T> Object newInstance(FCodecs codecs, CtClass clazz) throws Exception {
        return clazz.toClass().getConstructor(FCodecs.class).newInstance(codecs);
    }
    
    static <T> void makeCodecFieldsAndConstructor(FCodecs codecs, ClassPool pool, CtClass clazz) throws Exception {
        Map<String, FieldCodec<?, ?>> codeces = codecs.get();
        StringBuilder body = new StringBuilder("{");
        Set<String> keys = codeces.keySet();
        for (String key : keys) {
            CtField cf = CtField.make(makeCodecFieldBody(key), clazz);
            clazz.addField(cf);
            body.append(makeCodecAssignmentBody(key));
        }
        if(codecs.hasTypeParser()) {
            CtField cf = CtField.make(makeTypeParserFieldBody(), clazz);
            clazz.addField(cf);
            body.append(makeTypeParserAssignmentBody());
        }
        CtClass[] paramters = new CtClass[]{pool.get(FCodecs.class.getName())};
        CtConstructor ctConstructor = new CtConstructor(paramters, clazz);
        ctConstructor.setBody(body.append("}").toString());
        clazz.addConstructor(ctConstructor);
    }
    
    static String makeTypeParserFieldBody() {
        return new StringBuilder().append("private ").append(TypeParser.class.getName()).append(" ").append(typeParserFieldName()).append(";").toString();
    }
    static String makeTypeParserAssignmentBody() {
        return new StringBuilder().append("this.").append(typeParserFieldName()).append(" = $1.getTypeParser();").toString();
    }
    
    static String makeCodecFieldBody(String jfieldName) {
        return new StringBuilder().append("private ").append(FieldCodec.class.getName()).append(" ").append(codecFieldName(jfieldName)).append(";").toString();
    }
    static String makeCodecAssignmentBody(String jfieldName) {
        return new StringBuilder().append("this.").append(codecFieldName(jfieldName)).append(" = (").append(FieldCodec.class.getName()).append(")$1.get(\"").append(jfieldName).append("\");").toString();
    }
    
    @SuppressWarnings("unchecked")
    static <T> TypePSSetter<T> makeSetter(FTable ftable, List<FColumn> columns) throws Exception {
    	if(columns == null) return null;
    	
        String clazzName = ftable.clazz.getName();
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.makeClass(clazzName + "$TypePSSetter_" + (complieIndex()));
        clazz.addInterface(pool.getCtClass(TypePSSetter.class.getName()));
        
        
        StringBuilder body = new StringBuilder("{");
        body.append(clazzName).append(" _tmp_ = (").append(clazzName).append(") $2;");
        int index = 1;
        for (FColumn fColumn : columns) {
            body.append(TypeCodec.makeSetterBodyElement(ftable.codecs, ftable.jTable, fColumn, "_tmp_", "$1", index));
            ++index;
        }
        body.append("}");
        
        makeCodecFieldsAndConstructor(ftable.codecs, pool, clazz);//先解析columns才有codecFields, 先添加codecFields才能生成body
        
        CtMethod setter = CtNewMethod.copy(pool.getMethod(TypePSSetter.class.getName(), "set"), clazz, null);
        setter.setBody(body.toString());
        clazz.addMethod(setter);
        
        return (TypePSSetter<T>) newInstance(ftable.codecs, clazz);   
    }

    public static String typeParserFieldName() {
        return "_typeparser";
    }
    public static String codecFieldName(String jfieldName) {
        return jfieldName + "_codec";
    }
    
}

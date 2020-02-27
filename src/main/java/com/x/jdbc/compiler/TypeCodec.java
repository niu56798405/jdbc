package com.x.jdbc.compiler;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Objects;

import com.x.jdbc.analyzer.FCodecs;
import com.x.jdbc.analyzer.FColumn;
import com.x.jdbc.analyzer.JColumn;
import com.x.jdbc.analyzer.JTable;


/**
 * getter setter 解析
 * @author 
 */
class TypeCodec {
	
    static String specsName(String name) {
        char first = name.charAt(0);
        if (Character.isUpperCase(first)) {
            return name;
        }
        if (name.length() > 1) {
            char second = name.charAt(1);
            if (Character.isUpperCase(second)) {
                return name;
            }
        }
        return Character.toString(Character.toUpperCase(first)) + name.substring(1);
    }
    
    static String castingName(Class<?> c) {
        if(c.isArray()) {
            String end = "[]";
            Class<?> t = c.getComponentType();
            while(t.isArray()) {
                end += "[]";
                t = t.getComponentType();
            }
            return t.getName() + end;
        }
        return c.getName();
    }
    
	//setter
	static <T> String makeSetterBodyElement(FCodecs codecs, JTable jTable, FColumn fColumn, String type, String rs, int index) {
		StringBuilder sb = new StringBuilder();
		return sb
			.append(rs).append(".").append(psSetter(codecs, fColumn.jColumn)).append("(")
			.append(index).append(", ").append(columnVal(codecs, jTable, type, fColumn.jColumn))
			.append(");").toString();
	}
	
	static <T> String columnVal(FCodecs codecs, JTable jTable, String type, JColumn jColumn) {
	    String val = fieldVal(jTable, type, jColumn);
	    val = primitiveColumnCasting(jColumn.type, val);
	    if(codecs.hasCodec(jColumn.name)) {
	        return primitiveColumnCasting(
	        		codecs.findColumnTypeFromCodec(jColumn.name),
	        		String.format("this.%s.encode(%s)", CodeGenerator.codecFieldName(jColumn.name), primitiveEncodeCasting(jColumn.type, val)));
	    }
	    return val;
	}

	private static String fieldVal(JTable jTable, String type, JColumn jColumn) {
	    if(jColumn.isPrivate) {
	        String getter = jTable.getter.get("get" + jColumn.name.toLowerCase());
	        if(getter == null) {
	            getter = jTable.getter.get("is" + jColumn.name.toLowerCase());
	        }
	        if(getter == null && jColumn.name.startsWith("is")) {
	            getter = jTable.getter.get(jColumn.name.toLowerCase());
	        }
	        if(getter == null) {
	            getter = "get" + specsName(jColumn.name);
	        }
	        return type + "." + getter + "()";
	    }
	    return type + "." + jColumn.name;
	}
	
	private static String primitiveEncodeCasting(Class<?> c, String expr) {
		if(boolean.class.equals(c)) {
            return "Boolean.valueOf(" + expr + ")";
        } else if(byte.class.equals(c)) {
            return "Byte.valueOf(" + expr + ")";
        } else if(short.class.equals(c)) {
            return "Short.valueOf(" + expr + ")";
        } else if(int.class.equals(c)) {
            return "Integer.valueOf(" + expr + ")";
        } else if(long.class.equals(c)) {
            return "Long.valueOf(" + expr + ")";
        } else if(float.class.equals(c)) {
            return "Float.valueOf(" + expr + ")";
        } else if(double.class.equals(c)) {
            return "Double.valueOf(" + expr + ")";
        }
	    return expr;
	}
	
	static String primitiveColumnCasting(Class<?> c, String expr) {
	    expr = "((" + castingName(c) + ")" + expr + ")";
	    if(Boolean.class.equals(c)) {
	        return expr + " == null? false :" + expr + ".booleanValue()";
	    } else if(Byte.class.equals(c)) {
	        return expr + " == null? 0 :" + expr + ".byteValue()";
	    } else if(Short.class.equals(c)) {
	        return expr + " == null? 0 :" + expr + ".shortValue()";
	    } else if(Integer.class.equals(c)) {
	        return expr + " == null? 0 :" + expr + ".intValue()";
	    } else if(Long.class.equals(c)) {
	        return expr + " == null? 0 :" + expr + ".longValue()";
	    } else if(Float.class.equals(c)) {
	        return expr + " == null? 0 :" + expr + ".floatValue()";
	    } else if(Double.class.equals(c)) {
	        return expr + " == null? 0 :" + expr + ".doubleValue()";
	    }
	    return expr;
	}
	
	static <T> String psSetter(FCodecs codecs, JColumn jColumn) {
		Class<?> c = codecs.hasCodec(jColumn.name) ? codecs.findColumnTypeFromCodec(jColumn.name) : jColumn.type;
        if(Boolean.class.equals(c) || boolean.class.equals(c)) {
            return "setBoolean";
        } else if(Byte.class.equals(c) || byte.class.equals(c)) {
            return "setByte";
        } else if(Character.class.equals(c) || char.class.equals(c)) {
            return "setShort";
        } else if(Short.class.equals(c) || short.class.equals(c)) {
            return "setShort";
        } else if(c.isEnum() || Integer.class.equals(c) || int.class.equals(c)) {
            return "setInt";
        } else if(Long.class.equals(c) || long.class.equals(c)) {
            return "setLong";
        } else if(Float.class.equals(c) || float.class.equals(c)) {
            return "setFloat";
        } else if(Double.class.equals(c) || double.class.equals(c)) {
            return "setDouble";
        } else if(Byte[].class.equals(c) || byte[].class.equals(c)) {
            return "setBytes";
        } else if(BigDecimal.class.equals(c)) {
            return "setBigDecimal";
        } else if(Reader.class.equals(c)) {
            return "setCharacterStream";
        } else if(InputStream.class.equals(c)) {
            return "setBinaryStream";
        } else if(java.sql.Date.class.equals(c)) {
            return "setDate";
        } else if(Time.class.equals(c)) {
            return "setTime";
        } else if(java.util.Date.class.equals(c) || Timestamp.class.equals(c)) {
            return "setTimestamp";
        } else if(Clob.class.equals(c)) {
            return "setClob";
        } else if(Blob.class.equals(c)) {
            return "setBlob";
        } else if(Character[].class.equals(c) || String.class.equals(c) || char[].class.equals(c)) {
            return "setString";
        }
        return "setObject";
	}
	

	//parser
	static <T> String makeParserBodyElement(FCodecs codecs, JTable jTable, FColumn fColumn, String type, String rs, int index) {
		JColumn jColumn = fColumn.jColumn;
		String val = fieldVal(codecs, rs, jColumn, index);
		String string = jColumn.isPrivate ? setBySetter(type, jColumn, val) : setByAssign(type, jColumn, val);
		return string;
	}
	
	static String setBySetter(String type, JColumn jColumn, String val) {		
		StringBuilder sb = new StringBuilder(type);
		sb.append(".set").append(specsName(jColumn.name)).append("(");
	
		return sb.append(primitiveDecodeCasting(jColumn.type, val)).append(");").toString();
	}

	static String setByAssign(String type, JColumn jColumn, String get) {
		return new StringBuilder().append(type).append(".").append(jColumn.name).append("=").append(get).append(";").toString();
	}
	
	static <T> String fieldVal(FCodecs codecs, String rs, JColumn jColumn, int index) {
		String rsget = new StringBuilder().append(rs).append(".").append(rsGetter(codecs, jColumn)).append("(").append(index).append(")").toString();
		if(codecs.hasCodec(jColumn.name)) {
			return primitiveFieldCasting(
					jColumn.type, 
					String.format("this.%s.decode(%s)", CodeGenerator.codecFieldName(jColumn.name), primitiveDecodeCasting(codecs.findColumnTypeFromCodec(jColumn.name), rsget)));
		}
		return rsget;
	}

	static String primitiveFieldCasting(Class<?> c, String expr) {
		if(boolean.class.equals(c)) {
			return "((Boolean)" + expr + ").booleanValue()";
		} else if(byte.class.equals(c)) {
			return   "((Byte)" + expr + ").byteValue()";
		} else if(short.class.equals(c)) {
			return   "((Short)" + expr + ").shortValue()";
		} else if(int.class.equals(c)) {
			return "((Integer)" + expr + ").intValue()";
		} else if(long.class.equals(c)) {
			return    "((Long)" + expr + ").longValue()";
		} else if(float.class.equals(c)) {
			return   "((Float)" + expr + ").floatValue()";
		} else if(double.class.equals(c)) {
			return  "((Double)" + expr + ").doubleValue()";
		}
		return "(" + castingName(c) + ")" + expr;
	}
	
	static String primitiveDecodeCasting(Class<?> c, String expr) {
	    if(Boolean.class.equals(c)) {
            return " Boolean.valueOf(" + expr + ")";
        } else if(Byte.class.equals(c)) {
            return  " Byte.valueOf(" + expr + ")";
        } else if(Short.class.equals(c)) {
            return "Short.valueOf(" + expr + ")";
        } else if(Integer.class.equals(c)) {
            return " Integer.valueOf(" + expr + ")";
        } else if(Long.class.equals(c)) {
            return "Long.valueOf(" + expr + ")";
        } else if(Float.class.equals(c)) {
            return "Float.valueOf(" + expr + ")";
        } else if(Double.class.equals(c)) {
            return "Double.valueOf(" + expr + ")";
        }
	    return expr;
	}
	
	static <T> String rsGetter(FCodecs codecs, JColumn jColumn) {
	    Class<?> c = codecs.hasCodec(jColumn.name) ? codecs.findColumnTypeFromCodec(jColumn.name) : jColumn.type;
        if(Boolean.class.equals(c) || boolean.class.equals(c)) {
            return "getBoolean";
        } else if(Byte.class.equals(c) || byte.class.equals(c)) {
            return "getByte";
        } else if(Character.class.equals(c) || char.class.equals(c)) {
            return "getShort";
        } else if(Short.class.equals(c) || short.class.equals(c)) {
            return "getShort";
        } else if(Integer.class.equals(c) || int.class.equals(c)) {
            return "getInt";
        } else if(Long.class.equals(c) || long.class.equals(c)) {
            return "getLong";
        } else if(Float.class.equals(c) || float.class.equals(c)) {
            return "getFloat";
        } else if(Double.class.equals(c) || double.class.equals(c)) {
            return "getDouble";
        } else if(Byte[].class.equals(c) || byte[].class.equals(c)) {
            return "getBytes";
        } else if(BigDecimal.class.equals(c)) {
            return "getBigDecimal";
        } else if(Reader.class.equals(c)) {
            return "getCharacterStream";
        } else if(InputStream.class.equals(c)) {
            return "getBinaryStream";
        } else if(java.sql.Date.class.equals(c)) {
            return "getDate";
        } else if(Time.class.equals(c)) {
            return "getTime";
        } else if(java.util.Date.class.equals(c) || Timestamp.class.equals(c)) {
            return "getTimestamp";
        } else if(Clob.class.equals(c)) {
            return "getClob";
        } else if(Blob.class.equals(c)) {
            return "getBlob";
        } else if(Character[].class.equals(c) || String.class.equals(c) || char[].class.equals(c)) {
            return "getString";
        }
        return "getObject";
	}
	
}


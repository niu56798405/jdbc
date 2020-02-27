package com.x.jdbc;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.x.jdbc.analyzer.Analyzer;
import com.x.jdbc.analyzer.AnalyzerInfo;
import com.x.jdbc.analyzer.FTable;
import com.x.jdbc.codec.FieldCodec;
import com.x.jdbc.compiler.SQLCompiler;
import com.x.jdbc.compiler.SQLFactory;
import com.x.jdbc.compiler.SQLGenerator;
import com.x.jdbc.sequal.Sequal;


/**
 * 一一对应一张数据库表与一个java实体类(Entity)
 * 绑定一个数据库类型(每个类型对应一个连接池)
 * 
 * Java Entity
 * 对应Field为private时 使用Setter/Getter方法, 其他情况直接访问属性
 * 
 * @author 
 */
public class TypeQuery<T> {
	
    private transient AnalyzerInfo<T> info;
    
    private String tableName;
    //final compiled sqls
	private SQL<T>[] sqls;

	public static final int    ALL = 127;
	public static final int  QUERY = 1;
	public static final int INSERT = 2;
	public static final int DELETE = 4;
	public static final int UPDATE = 8;
	public static final int INSTUP = 16;//insert update
	
	private static Set<TypeQuery<?>> queries = new HashSet<>(); 
	
	private int without;
	private boolean instupPrior;
	
	public TypeQuery(Class<T> clazz) {
		this.info = new AnalyzerInfo<T>(clazz);
		this.instupPrior = Configuration.isInstupPrior();
		queries.add(this);
	}
	
	public TypeQuery<T> configureInstupPrior(boolean instupPrior) {
	    this.instupPrior = instupPrior;
	    return this;
	}
	
	public TypeQuery<T> configureAsyncModel(boolean asyncModel) {
	    info.configureAsyncModel(asyncModel);
	    return this;
	}
	
	/**
	 * 不自动生成哪些类型的SQL
	 * Demo:
	 *     query.without(TypeQuery.QUERY | TypeQuery.INSERT | TypeQuery.DELETE | TypeQuery.UPDATE)
	 * @param without
	 * @return
	 */
	public TypeQuery<T> without(int without) {
	    this.without = without;
	    return this;
	}
	
	/**
	 * 联合查询
	 * @param fieldName
	 * @param tableName
	 * @return 对应该表的TypeQuery
	 */
	public TypeQuery<T> associate(String fieldName, String tableName) {
		//FIXME
		return this;
	}
	
	/**
	 * 映射Java实体字段与数据库字段
	 * @param fieldName Java实体字段
	 * @param columnName 数据库字段
	 * @return
	 */
	public TypeQuery<T> mapping(String fieldName, String columnName) {
	    info.mapping(fieldName, columnName);
		return this;
	}
	
	/**
	 * 绑定datasource类型
	 * @param type
	 * @return
	 */
	public TypeQuery<T> binding(Enum<?> type) {
	    info.binding(type);
		return this;
	}
	
	public TypeQuery<T> binding(Enum<?> type, String tableName) {
	    this.tableName = tableName;
	    
	    info.binding(type, tableName);
		return this;
	}
	
	public TypeQuery<T> binding(JDBCTemplate jdbcTemplate, String tableName) {
        this.tableName = tableName;
        
        info.binding(jdbcTemplate, tableName);
        return this;
    }
	
	public TypeQuery<T> binding(JDBCTemplate jdbcTemplate) {
	    info.binding(jdbcTemplate);
        return this;
    }
    
	public TypeQuery<T> binding(int idx, String sql) {
	    return binding(idx, Sequal.origin(sql));
	}
	
	public TypeQuery<T> binding(int idx, Sequal sequal) {
	    info.binding(idx, sequal);
	    return this;
	}
	
	public TypeQuery<T> binding(String fieldName, FieldCodec<?, ?> codec) {
	    info.binding(fieldName, codec);
	    return this;
	}
	
	public TypeQuery<T> binding(TypeParser<T> typeParser) {
	    info.binding(typeParser);
	    return this;
	}
	
	public TypeQuery<T> binding(Class<?> fieldType, FieldCodec<?, ?> codec) {
        info.binding(fieldType, codec);
        return this;
    }
	
	public SQL<T> load(int idx) {
		return compile().check(sqls[idx]);
	}
	
	private SQL<T> qryall;
	public List<T> fetchall() {
		return compile().check(qryall).fetchMany(PSSetter.NONE);
	}
	
	private SQL<T> qrykey;
	public T fetchoneByKey(Object key) {
		return compile().check(qrykey).fetchOneByKey(key);
	}
	public T fetchoneByKey(PSSetter key) {
	    return compile().check(qrykey).fetchOneByKey(key);
	}
	
	private SQL<T> insert;
	public boolean insert(T obj) {
		return compile().check(instupInstead(insert)).update(obj);
	}
	public long insertAndGenKey(T obj) {
        return compile().check(insert).insertAndGenKey(obj);
    }
	public boolean insertBatch(Collection<T> objs) {
        return compile().check(instupInstead(insert)).updateBatch(objs);
    }
	public boolean insertBatch(T[] objs) {
	    return compile().check(instupInstead(insert)).updateBatch(objs);
	}
	
	private SQL<T> instup;
    public boolean instup(T obj) {
        return compile().check(instup).update(obj);
    }
    public boolean instupBatch(T[] objs) {
        return compile().check(instup).updateBatch(objs);
    }
    public boolean instupBatch(Collection<T> objs) {
        return compile().check(instup).updateBatch(objs);
    }
	
	private SQL<T> update;
	public boolean update(T obj) {
	    return compile().check(instupInstead(update)).update(obj);
	}
	public boolean updateBatch(T[] objs) {
	    return compile().check(instupInstead(update)).updateBatch(objs);
	}
	public boolean updateBatch(Collection<T> objs) {
	    return compile().check(instupInstead(update)).updateBatch(objs);
	}
	/*same with update*/
    public boolean updateByKey(T obj) {
        return compile().check(instupInstead(update)).update(obj);
    }
    public boolean updateBatchByKey(T[] objs) {
        return compile().check(instupInstead(update)).updateBatch(objs);
    }
    public boolean updateBatchByKey(Collection<T> objs) {
        return compile().check(instupInstead(update)).updateBatch(objs);
    }
	
	private SQL<T> delete;
	public boolean delete(T obj) {
	    return compile().check(delete).update(obj);
	}
	public boolean deleteByKey(Object key) {
		return compile().check(delete).updateByKey(key);
	}
	public boolean deleteByKey(PSSetter setter) {
	    return compile().check(delete).update(setter);
	}
	
    private SQL<T> instupInstead(SQL<T> src) {
        if(instupPrior && instup != null) {
            return instup;
        }
        return src;
    }
	
    private SQL<T> check(SQL<T> sql) {
        if(tableName == null) {
            throw new IllegalArgumentException("None binding table");
        }
        if(sql == null) {
            throw new IllegalArgumentException("Current SQL is None, Make sure the table:[" + tableName + "] has primary key and SQL not int the 'without' settings");
        }
        return sql;
    }
    
    public TypeQuery<T> compile() {
        if(this.info == null) return this;
        synchronized(this.info) {
            if(this.info == null) return this;//double check
            
            try {
                FTable ftable = Analyzer.analyze(info);
                SQLFactory<T> factory = info.buildFactory();
                
                if((without & QUERY) == 0) {
                    this.qryall = SQLGenerator.genQuerySQL(factory, ftable);
                    this.qrykey = SQLGenerator.genQryKeySQL(factory, ftable, qryall);
                }

                if((without & INSERT) == 0)
                    this.insert = SQLGenerator.genInsertSQL(factory, ftable);

                if((without & INSTUP) == 0)
                    this.instup = SQLGenerator.genInstupSQL(factory, ftable, insert);

                if((without & DELETE) == 0)
                    this.delete = SQLGenerator.genDeleteSQL(factory, ftable);

                if((without & UPDATE) == 0)
                    this.update = SQLGenerator.genUpdateSQL(factory, ftable);

                this.sqls = SQLCompiler.compile(info.sequals(), factory, ftable);

                this.info = null;
             
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }    
        }
        return this;
    }
    
    public static void doCompile() {
        queries.forEach(TypeQuery::compile);
    }
    
    public static void shutdown() {
        Configuration.shutdown();
    }

}

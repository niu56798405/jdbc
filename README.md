## nebula-jdbc
- jdbc的简单封装(JDBCTempalte)
- JavaType与Table/SQLResult的映射(TypeQuery)
- 基于jdbc-pool的DataSource

-----
#### JDBCTemplate
```java
JDBCTemplate jdbc = new JDBCTemplate(DataSource);
jdbc.fetchOne(sql, PSSetter, RSParser<T>);//T
jdbc.fetchMany(sql, PSSetter, RSParser<T>);//List<T>
jdbc.update(sql, PSSetter);//Boolean
jdbc.updateBatch(sql, PSSetter...);//Boolean
jdbc.updateBatch(sql, Collection(PSSetter));//Boolean
jdbc.fetchCount(sql, PSSetter);//long
jdbc.fetchIncrement(sql);//long
jdbc.execute(sql);//Boolean
jdbc.executeBatch(Collection<sql>);//Boolean

PSSetter: Setter for PreparedStatement
RSParser: Parser for ResultSet
```

#### TypeQuery
```java
TypeQuery<T> query = new TypeQuery<T>(T.class)
	.binding(JdbcTemplate, TableName)
	.binding(0, selectSQL)//第一个参数设计为数组的下标值,尽量连续
	.binding(1, Customizer.updateWith(wheres))
	.without(TypeQuery.INSERT | TypeQuery.DELETE)//可以不生成某些SQL
	.compile();
query.fetchall();//List<T>
query.fetchoneByKey(Key);//T
query.updateByKey(T);//Boolean
query.deleteByKey(Key);//Boolean
query.insert(T);//Boolean
query.instup(T);//Boolean (Insert,如果已有相同记录则使用Update)
query.load(0).fetchMany(PSSetter);
query.load(1).update(T);

ByKey: Need a single column primary key in the table

@Codec JavaType中可以使用@Codec注释 自动转换JAVA类型与SQL类型
对应关系:
---------------------------------------------------------------
JAVA TYPE |SQL TYPE| @Codec.value      | Comment  ()内为默认值
----------|--------|-------------------|-----------------------
Enum      |Integer | 0                 | int value
Array     |Varchar | ',' | '|'<<8      | Int/Long/String(分隔符 ,|)
Collection|Varchar | ','               | List/Set(分隔符 ,)
---------------------------------------------------------------
只有需要调整默认值时才需要加注解@Codec(Enum除外), 字段名对应上时会使用默认配置进行转换
```

#### DataSource
```java
DBConf conf = new DBConf();//database config
DataSource dataSource = JdbcPool.buildDataSource(conf);//use default configuration
```
#### Configuration
```java
Configuration.configureAsyncModel(int nThreads);//async threads
TypeQuery中生成的除Read外的SQL将使用异步模式
```
#### TOOLS
```java
//解析SQL脚本成独立的SQL语句,方便执行
SQLScript.parse(File);//List<String> sqls;
SQLScript.parse(text);//List<String> sqls;
```


package com.x.jdbc.datasource;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;

public class JdbcPool {
    
    private static String DBURL_TEMPLATE = "jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&autoReconnect=true&useServerPrepStmts=true&rewriteBatchedStatements=true";
    public static DataSource buildDataSource(DBConf conf) {
        return new org.apache.tomcat.jdbc.pool.DataSource(buildJdbcPoolConf(conf));
    }
    public static String toDBURL(DBConf conf) {
        return String.format(DBURL_TEMPLATE, conf.host, conf.port, conf.name);
    }
    private static PoolProperties buildJdbcPoolConf(DBConf conf) {
        PoolProperties p = new PoolProperties();
        p.setUrl(toDBURL(conf));
        p.setUsername(conf.user);
        p.setPassword(conf.pass);
        p.setJmxEnabled(true);
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setValidationQuery("SELECT 1;");
        p.setTestWhileIdle(true);
        p.setTestOnBorrow(false);
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(conf.maxconn);
        p.setInitialSize(conf.minconn);
        p.setMinIdle(conf.minconn);
        p.setMaxIdle(conf.maxconn);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors("ConnectionState;StatementFinalizer;StatementCache(max=1024)");
//        p.setJdbcInterceptors("ConnectionState;StatementFinalizer;StatementCache(max=50);SlowQueryReport;SlowQueryReportJmx");
        return p;
    }

}

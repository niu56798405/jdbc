package com.x.jdbc.compiler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.x.jdbc.Configuration;
import com.x.jdbc.SQL;
import com.x.jdbc.analyzer.FColumn;
import com.x.jdbc.analyzer.FIndex;
import com.x.jdbc.analyzer.FTable;
import com.x.jdbc.parser.ParseRet;
import com.x.jdbc.parser.SQLParser;
import com.x.jdbc.sequal.Sequal;


/**
 * compile binding sqls
 * @author 
 */
public class SQLCompiler {
    
    private static final Logger logger = LoggerFactory.getLogger(SQLCompiler.class);
    
    @SuppressWarnings("unchecked")
    public static <T> SQL<T>[] compile(Sequal[] sequals, SQLFactory<T> factory, FTable ftable) throws Exception {
        SQL<T>[] csqls = new SQL[sequals.length];
        for (int i = 0; i < sequals.length; i++) {
            Sequal customize = sequals[i];
            if(customize == null) continue;
            csqls[i] = compile(factory, ftable, customize.dependence(ftable).toSql());
        }
        return csqls;
    }

    //check where columns is index or unique key
    static <T> boolean avaliable(FTable ftable, List<FColumn> whereColumns, boolean onlyUnique) {
        if(whereColumns.isEmpty()) {
            return true;
        }
        for(FIndex index : ftable.indexs) {
            for (FColumn fColumn : index.columns) {
                if(whereColumns.contains(fColumn)) {
                    return true;
                }
            }
        }
        return false;
    }

    static <T> SQL<T> compile(SQLFactory<T> factory, FTable ftable, String sql) throws Exception {
        ParseRet pr = new SQLParser(ftable, sql).parse();
        if(!avaliable(ftable, pr.whereColumns, ftable.hasOnlyOneUniqueIndex())) {
            if(Configuration.isCustomForceUseIndex()) {
                throw new IllegalArgumentException(String.format("sql[%s] where columns can`t use table index", sql));
            }
            logger.warn("sql[{}] where columns can`t use table index", sql);
        }
        return factory.newSQL(pr.sql, pr.whereColumns.size(), ftable.batchLimit(pr.setColumns.size()), CodeGenerator.makeSetter(ftable, pr.setColumns), CodeGenerator.makeParser(ftable, pr.getColumns));
    }

}

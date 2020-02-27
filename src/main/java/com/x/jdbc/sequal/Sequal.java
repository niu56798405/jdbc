package com.x.jdbc.sequal;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.x.jdbc.analyzer.FColumn;
import com.x.jdbc.analyzer.FTable;

public class Sequal {

	private static final String COMMA = ",";
	private static final String AND = "AND";
	private static final String OR = "OR";
	
	private static final String COUNT = "COUNT";
	private static final String SUM = "SUM";
	private static final String MAX = "MAX";
	private static final String DISTINCT = "DISTINCT";
	
	private static final Function<String, String> ID = s->s;
	private static final Function<String, String> EQ = s->s+"=?";
	private static final Function<String, String> NEQ = s->s+"<>?";
	private static final Function<String, String> MORE = s->s+">?";
	private static final Function<String, String> LESS = s->s+"<?";

	static enum Option {
		ORIGIN, SELECT, DELETE, UPDATE;
	}

	public static Sequal origin(String sql) {
    	return new Sequal(sql);
    }
    
    public static Sequal query() {
    	return new Sequal(Option.SELECT);
    }
    
    public static Sequal build() {
        return new Sequal();
    }
    
	private Option option;
	private String sql;
	
	private SequalPart opt;
	private SequalPart cnd;
	private SequalPart ret;

	private Sequal() {
	    opt = new SequalPart();
	    cnd = new SequalPart();
	    ret = new SequalPart();
	}
	private Sequal(Option option) {
	    this();
		this.option = option;
		this.sql = null;
	}
	private Sequal(String sql) {
		this.option = Option.ORIGIN;
		this.sql = sql;
	}

	public Sequal as(String alias) {
		opt.append(" AS ").append(alias);
		return this;
	}
	
	public Sequal distinct(String... columnNames) {
		Arrays.stream(columnNames).forEach(opt.functional(DISTINCT).concat(COMMA).assign(ID));
		return this;
	}
	
	public Sequal max(String... columnNames) {
		Arrays.stream(columnNames).forEach(opt.functional(MAX).concat(COMMA).assign(ID));
		return this;
	}
	
	public Sequal sum(String... columnNames) {
		Arrays.stream(columnNames).forEach(opt.functional(SUM).concat(COMMA).assign(ID));
		return this;
	}
	
	public Sequal count(String... columnNames) {
		Arrays.stream(columnNames).forEach(opt.functional(COUNT).concat(COMMA).assign(ID));
		return this;
	}

	public Sequal where(String... columnNames) {
		Arrays.stream(columnNames).forEach(cnd.concat(AND).assign(EQ));
		return this;
	}
	public Sequal whereNot(String... columnNames) {
	    Arrays.stream(columnNames).forEach(cnd.concat(AND).assign(NEQ));
	    return this;
	}
	public Sequal whereLess(String... columnNames) {
	    Arrays.stream(columnNames).forEach(cnd.concat(AND).assign(LESS));
	    return this;
	}
	public Sequal whereMore(String... columnNames) {
	    Arrays.stream(columnNames).forEach(cnd.concat(AND).assign(MORE));
	    return this;
	}
	
	public Sequal select(String... columnNames) {
	    this.option = Option.SELECT;
	    Arrays.stream(columnNames).forEach(cnd.concat(COMMA).assign(ID).functional(null));
	    return this;
	}

	public Sequal update(String... columnNames) {
	    this.option = Option.UPDATE;
	    Arrays.stream(columnNames).forEach(opt.concat(COMMA).assign(EQ).functional(null));
	    return this;
	}
	
	public Sequal delete() {
	    this.option = Option.DELETE;
	    return this;
	}
	
	public Sequal or(String... columnNames) {
		Arrays.stream(columnNames).forEach(cnd.concat(OR).assign(EQ));
		return this;
	}

	public Sequal orderby(String... columnNames) {
		assert columnNames.length > 0;
		ret.append(" ORDER BY");
		Arrays.stream(columnNames).forEach(ret.functional(null).concat(COMMA).assign(ID));
		ret.append(" ASC");
		return this;
	}

	public Sequal orderbyDesc(String... columnNames) {
		assert columnNames.length > 0;
		ret.append(" ORDER BY");
		Arrays.stream(columnNames).forEach(ret.functional(null).concat(COMMA).assign(ID));
		ret.append(" DESC");
		return this;
	}

	public Sequal groupby(String... columnNames) {
		assert columnNames.length > 0;
		ret.append(" GROUP BY");
		Arrays.stream(columnNames).forEach(ret.functional(null).concat(COMMA).assign(ID));
		return this;
	}

	public Sequal limit(int from, int limit) {
		ret.append(" LIMIT ").append(String.valueOf(from)).append(",").append(String.valueOf(limit));
		return this;
	}
	
	private FTable table;
	public Sequal dependence(FTable table) {
		this.table = table;
		return this;
	}

	public String toSql() {
		switch (option) {
		case ORIGIN:
			return sql;
		case SELECT:
			return String.format("SELECT %s FROM %s WHERE %s %s", opt.defaults(()->"*"), table.tableName, cnd, ret); 
		case DELETE:
			assert cnd.isEmpty() == false;
			return String.format("DELETE FROM %s WHERE %s", table.tableName, cnd);
		case UPDATE:
			assert cnd.isEmpty() == false;
			return String.format("UPDATE %s SET %s WHERE %s", table.tableName, opt.defaults(()->updateDefaults(table.columns)), cnd);
		default:
			throw new IllegalArgumentException("None usable option {SELECT | DELETE | UPDATE}");
		}
	}

    protected String updateDefaults(List<FColumn> columns) {
        SequalPart part = new SequalPart();
        columns.stream().map(c->c.dbColumn.name).forEach(part.concat(COMMA).assign(EQ));
        return part.toString();
    }
	
	class SequalPart implements Consumer<String> {
		protected StringBuilder part = new StringBuilder();
		protected boolean accepted = false;
		protected Supplier<String> defaults;
		protected String concat;
		protected String func;
		protected Function<String, String> assign;
		
		public SequalPart() {
			this.defaults = ()->"";
		}
		private String trim(String src) {
		    if(src == null || src.length() == 0) return "";
		    int start = 0, end = src.length();
		    while ((start < end) && src.charAt(start) == '`') ++ start;
		    while ((start < end) && src.charAt(end-1) == '`') -- end;
		    return end > start ? src.substring(start, end) : "";
		}
		@Override
		public final void accept(String c) {
			part.append(" ").append(concat()).append(" ").append(funcPrefix()).append(assign.apply(trim(c))).append(funcSuffix());
			accepted = true;
		}
        protected String concat() {
			return accepted ? concat : "";
		}
		public SequalPart concat(String concat) {
			this.concat = concat;
			return this;
		}
		private String funcPrefix() {
			return func == null ? "" : func + "(";
		}
		private String funcSuffix() {
			return func == null ? "" : ")";
		}
		public SequalPart functional(String func){
			this.func = func;
			return this;
		}
		public SequalPart assign(Function<String, String> assign) {
		    this.assign = assign;
		    return this;
		}
		public SequalPart append(String s) {
			this.part.append(s);
			return this;
		}
		public SequalPart defaults(Supplier<String> defaults) {
			if(defaults != null) this.defaults = defaults;
			return this;
		}
		public boolean isEmpty() {
			return part.length() == 0;
		}
		@Override
		public String toString() {
			return isEmpty() ? defaults.get() : part.toString().trim();
		}
	}
	
}

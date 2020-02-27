package com.x.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * 
 * @author 
 *
 * @param <T>
 */
public interface TypeParser<T> {
    
    T make(ResultSet rs) throws SQLException ;
    void apply(T t);
    
    public static <T> Lambda<T> buildBySupplier(TypeSupplier<T> supplier) {
        return new Lambda<>(supplier, t->{});
    }
    public static <T> Lambda<T> buildByConsumer(Consumer<T> consumer) {
        return new Lambda<>(rs->null, consumer);
    }
    public static <T> Lambda<T> build(TypeSupplier<T> supplier, Consumer<T> consumer) {
        return new Lambda<>(supplier, consumer);
    }
    
    interface TypeSupplier<T> {
        T make(ResultSet rs) throws SQLException;
    }
    
    class Lambda<T> implements TypeParser<T> {
        final TypeSupplier<T> supplier;
        final Consumer<T> consumer;
        public Lambda(TypeSupplier<T> supplier, Consumer<T> consumer) {
            this.supplier = supplier;
            this.consumer = consumer;
        }
        @Override
        public T make(ResultSet rs) throws SQLException {
            return supplier.make(rs);
        }
        @Override
        public void apply(T t) {
            consumer.accept(t);
        }
    }

}

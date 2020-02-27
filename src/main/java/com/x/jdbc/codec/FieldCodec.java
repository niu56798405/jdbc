package com.x.jdbc.codec;

import java.util.function.Function;

public interface FieldCodec<F, C> {

    C encode(F fieldValue);
    F decode(C columnValue);
    
    public static <F, C> FieldCodec<F, C> build(Function<C, F> decoder) {
        return new FieldCodec<F, C>() {
            public C encode(F fieldValue) {
                throw new UnsupportedOperationException();
            }
            public F decode(C columnValue) {
                return decoder.apply(columnValue);
            }
        };
    }
    
    public static <F, C> FieldCodec<F, C> build(Function<F, C> encoder, Function<C, F> decoder) {
        return new FieldCodec<F, C>() {
            public C encode(F fieldValue) {
                return encoder.apply(fieldValue);
            }
            public F decode(C columnValue) {
                return decoder.apply(columnValue);
            }
        };
    }
    
}

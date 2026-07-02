package com.example.carnest.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String action();
    int limit();
    int windowSeconds();
    KeyType keyType() default KeyType.USER;

    enum KeyType { USER, IP }
}

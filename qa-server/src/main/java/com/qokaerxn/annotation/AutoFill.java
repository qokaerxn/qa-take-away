package com.qokaerxn.annotation;


import com.qokaerxn.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //指定mapper对数据库操作类型 INSERT UPDATE
    OperationType value();
}

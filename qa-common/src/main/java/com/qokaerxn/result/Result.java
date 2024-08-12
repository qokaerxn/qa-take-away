package com.qokaerxn.result;


import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(){
        Result<T> result = new Result<T>();
        result.code=1;
        return result;
    }

    public static <T> Result<T> success(T data){
        Result<T> result = new Result<T>();
        result.data= data;
        result.code= 1;
        return result;
    }

    public static <T> Result<T> error(String failedMessage){
        Result<T> result = new Result<T>();
        result.message = failedMessage;
        result.code = 0;
        return result;
    }

}

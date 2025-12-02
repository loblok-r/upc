package cn.loblok.upc.dto;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "ok";
        r.data = null;
        return r;
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "ok";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        r.data = null;
        return r;
    }

    public static <T> Result<T> error(String msg,T data) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        r.data = data;
        return r;
    }
}
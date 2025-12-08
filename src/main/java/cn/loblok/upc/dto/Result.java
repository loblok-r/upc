package cn.loblok.upc.dto;

import cn.loblok.upc.enums.CommonStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    public static <T>Result success(T data){
        return new Result().setCode(CommonStatusEnum.SUCCESS.getCode()).setMsg(CommonStatusEnum.SUCCESS.getMessage()).setData(data);
    }

    public static <T> Result error(T data) {
       return new Result<>().setData(data);
    }

    public static <T>Result error(int code,String message){
        return new Result().setCode(code).setMsg(message);
    }

    public static <T>Result error(int code,String message,String data){
        return new Result().setCode(code).setMsg(message).setData(data);
    }

}
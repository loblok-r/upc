package cn.loblok.upc.common.base;

import cn.loblok.upc.common.enums.CommonStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用返回结果
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class Result<T> {
    @Schema(description = "状态码，200-成功，500-失败")
    private int code;
    @Schema(description = "返回信息")
    private String msg;
    @Schema(description = "返回数据")
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
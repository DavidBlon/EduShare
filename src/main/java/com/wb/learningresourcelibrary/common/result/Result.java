package com.wb.learningresourcelibrary.common.result;

import lombok.Data;

/**
 * 统一接口返回封装
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    private Result() {}

    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        return r;
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    /**
     * 成功响应（仅消息，无数据）
     */
    public static Result<Void> success(String msg) {
        Result<Void> r = new Result<>();
        r.code = 200;
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> success(String msg, T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.msg = msg;
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> unauthorized(String msg) {
        return error(401, msg);
    }

    public static <T> Result<T> forbidden(String msg) {
        return error(403, msg);
    }

    public static <T> Result<T> notFound(String msg) {
        return error(404, msg);
    }

    public static <T> Result<T> badRequest(String msg) {
        return error(400, msg);
    }
}

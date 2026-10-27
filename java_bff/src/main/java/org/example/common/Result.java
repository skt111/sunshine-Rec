package org.example.common;

import java.io.Serializable;

/**
 * 统一响应壳：{@code {status, message, result}}。
 *
 * <p>status 取值：success / fail / 401 / 403 / 304（与现有前端拦截器口径一致）。</p>
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;
    private String message;
    private T result;

    public Result() {
    }

    public Result(String status, String message, T result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>("success", "", data);
    }

    public static <T> Result<T> success() {
        return new Result<>("success", "", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>("fail", message, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>("401", "未登录", null);
    }

    public static <T> Result<T> forbidden() {
        return new Result<>("403", "无权限", null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}

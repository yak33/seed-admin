package io.github.yak33.common.core.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应结构（code=200 成功 / 500 失败）
 * <p>前端 vben request.ts 的 successCode 已同步配置为 200</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 成功状态码 */
    public static final int SUCCESS = 200;
    /** 失败状态码 */
    public static final int FAIL = 500;

    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> R<T> fail(String msg) {
        return restResult(null, FAIL, msg);
    }

    public static <T> R<T> fail(int code, String msg) {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}

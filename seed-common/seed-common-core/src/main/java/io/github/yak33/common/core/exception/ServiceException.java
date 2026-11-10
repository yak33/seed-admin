package io.github.yak33.common.core.exception;

import lombok.Data;

import java.io.Serial;

/**
 * 业务异常：带有明确业务语义的错误，由全局异常处理器转成 R.fail 响应
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer code;

    public ServiceException(String message) {
        super(message);
        this.code = null;
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

package io.github.yak33.common.web.handler;

import io.github.yak33.common.core.domain.R;
import io.github.yak33.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：业务异常带语义返回，未知异常统一 500 并记录日志
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：消息面向用户展示，直接透传
     */
    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        return e.getCode() == null ? R.fail(e.getMessage()) : R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验失败（@RequestBody @Valid）：取第一条校验错误提示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("参数校验失败");
        return R.fail(message);
    }

    /**
     * 参数校验失败（表单绑定）
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .orElse("参数校验失败");
        return R.fail(message);
    }

    /**
     * 静态资源/路径不存在：返回 404 语义，避免被兜底 500 掩盖
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoResourceFound(NoResourceFoundException e) {
        return R.fail(404, "请求地址不存在");
    }

    /**
     * 兜底异常：细节只进日志，不暴露给前端
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("未知异常：{}", e.getMessage(), e);
        return R.fail("系统异常，请联系管理员");
    }
}

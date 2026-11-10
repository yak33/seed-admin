package io.github.yak33.common.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import io.github.yak33.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 异常统一处理。
 * <p>未登录返回 HTTP 401（vben 前端拦截器依赖 401 状态码触发重新认证弹窗），
 * 无权限返回 HTTP 200 + code 403（前端仅提示无权限）</p>
 * <p>@Order 必须高于 GlobalExceptionHandler：advice 按序遍历且首个匹配即短路，
 * 兜底 handler 在前会把这些异常吞成 500</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<Void>> handleNotLogin(NotLoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(R.fail(HttpStatus.UNAUTHORIZED.value(), "登录状态已过期，请重新登录"));
    }

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public R<Void> handleNotPermission(Exception e) {
        return R.fail(403, "没有对应的操作权限");
    }
}

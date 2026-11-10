package io.github.yak33.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yak33.common.core.domain.R;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.bo.LoginBody;
import io.github.yak33.system.domain.vo.LoginVo;
import io.github.yak33.web.service.SysLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口（路径对齐 vben 前端契约 /auth/*）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Valid
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysLoginService loginService;

    /**
     * 账号密码登录
     */
    @PostMapping("/login")
    public R<LoginVo> login(@Valid @RequestBody LoginBody body) {
        return R.ok(loginService.login(body));
    }

    /**
     * 退出登录
     */
    @SaCheckLogin
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok();
    }

    /**
     * 获取当前用户权限码（vben 用于按钮级权限指令）
     */
    @SaCheckLogin
    @GetMapping("/codes")
    public R<List<String>> getCodes() {
        List<String> permissions = LoginHelper.getLoginUser().getPermissions();
        return R.ok(permissions == null ? List.of() : permissions);
    }
}

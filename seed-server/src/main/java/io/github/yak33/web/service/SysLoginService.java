package io.github.yak33.web.service;

import cn.dev33.satoken.stp.StpUtil;
import io.github.yak33.common.core.constant.Constants;
import io.github.yak33.common.core.domain.model.LoginUser;
import io.github.yak33.common.core.exception.ServiceException;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.SysUser;
import io.github.yak33.system.domain.bo.LoginBody;
import io.github.yak33.system.domain.vo.LoginVo;
import io.github.yak33.system.service.ISysMenuService;
import io.github.yak33.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录流程编排：账号密码校验 → 组装 LoginUser → 写入 Sa-Token 会话。
 * <p>骨架期只支持账号密码登录，多渠道（短信/社交）以后按需扩展</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysLoginService {

    private final ISysUserService userService;
    private final ISysMenuService menuService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录成功返回 token；副作用：创建 Sa-Token 会话
     */
    public LoginVo login(LoginBody body) {
        SysUser user = userService.selectUserByUserName(body.getUsername());
        // 账号不存在与密码错误返回同一提示，避免暴露账号是否存在
        if (user == null || !passwordEncoder.matches(body.getPassword(), user.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        if (!Constants.STATUS_NORMAL.equals(user.getStatus())) {
            throw new ServiceException("账号已停用，请联系管理员");
        }

        List<String> roleKeys = userService.selectRoleKeysByUserId(user.getUserId());
        List<String> permissions = menuService.selectPermsByUserId(user.getUserId(),
            roleKeys.contains(Constants.SUPER_ROLE_KEY));

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUserName(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setRoleKeys(roleKeys);
        loginUser.setPermissions(permissions);

        LoginHelper.login(loginUser);
        log.info("用户登录成功：userId={}, userName={}", user.getUserId(), user.getUserName());

        LoginVo vo = new LoginVo();
        vo.setAccessToken(StpUtil.getTokenValue());
        vo.setExpireIn(StpUtil.getTokenTimeout());
        return vo;
    }

    /**
     * 退出登录；副作用：当前 token 立即失效
     */
    public void logout() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        LoginHelper.logout();
        if (loginUser != null) {
            log.info("用户退出登录：userId={}, userName={}", loginUser.getUserId(), loginUser.getUserName());
        }
    }
}

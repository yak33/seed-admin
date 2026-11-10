package io.github.yak33.common.satoken.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import io.github.yak33.common.core.domain.model.LoginUser;

/**
 * 登录上下文工具：登录/登出、从 TokenSession 读写 LoginUser
 * <p>token 有效期 30 天，30 分钟无操作自动过期（活跃续期）</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
public final class LoginHelper {

    public static final String LOGIN_USER_KEY = "loginUser";

    private LoginHelper() {
    }

    /**
     * 登录并写入用户上下文
     * 副作用：生成新 token、创建 TokenSession
     */
    public static void login(LoginUser loginUser) {
        SaLoginParameter parameter = new SaLoginParameter()
            .setTimeout(30 * 24 * 60 * 60)
            .setActiveTimeout(30 * 60);
        StpUtil.login(loginUser.getUserId(), parameter);
        StpUtil.getTokenSession().set(LOGIN_USER_KEY, loginUser);
    }

    /**
     * 退出当前会话
     * 副作用：使当前 token 立即失效
     */
    public static void logout() {
        StpUtil.logout();
    }

    /**
     * 退出指定用户的全部会话
     * 副作用：该用户所有 token 立即失效
     */
    public static void logoutByUserId(String userId) {
        if (userId != null) {
            StpUtil.logout(userId);
        }
    }

    /**
     * 获取当前登录用户（未登录时抛 NotLoginException）
     */
    public static LoginUser getLoginUser() {
        SaSession session = StpUtil.getTokenSession();
        return (LoginUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取当前登录用户 id（未登录时抛 NotLoginException）
     */
    public static String getUserId() {
        return StpUtil.getLoginIdAsString();
    }

    /**
     * 获取当前登录用户 id，未登录返回 null。
     * 供 create_by/update_by 自动填充等容错场景使用
     */
    public static String getUserIdOrNull() {
        try {
            return StpUtil.getLoginIdAsString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断当前会话是否已登录（给非 Web 场景做前置判断）
     */
    public static boolean isLoggedIn() {
        return StpUtil.isLogin();
    }
}

package io.github.yak33.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yak33.common.core.domain.R;
import io.github.yak33.common.core.domain.model.LoginUser;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.SysUser;
import io.github.yak33.system.domain.vo.UserInfoVo;
import io.github.yak33.system.service.ISysMenuService;
import io.github.yak33.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户信息接口（vben 契约 GET /user/info）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@RequiredArgsConstructor
@RestController
public class UserController {

    private final ISysUserService userService;
    private final ISysMenuService menuService;

    /**
     * 获取当前登录用户信息：基础信息取实时库值（头像/昵称修改后即时生效），
     * 角色取登录会话缓存
     */
    @SaCheckLogin
    @GetMapping("/api/user/info")
    public R<UserInfoVo> getUserInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUser user = userService.selectUserByUserName(loginUser.getUserName());
        if (user == null) {
            return R.fail("用户不存在或已被删除");
        }

        UserInfoVo vo = new UserInfoVo();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUserName());
        vo.setRealName(user.getNickName());
        vo.setAvatar(user.getAvatar());
        vo.setRoles(loginUser.getRoleKeys());
        vo.setHomePath(menuService.selectHomePathByUserId(
            loginUser.getUserId(), loginUser.isSuper()));
        return R.ok(vo);
    }
}

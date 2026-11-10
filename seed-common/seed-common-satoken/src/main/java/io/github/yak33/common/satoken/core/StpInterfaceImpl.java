package io.github.yak33.common.satoken.core;

import cn.dev33.satoken.stp.StpInterface;
import io.github.yak33.common.core.domain.model.LoginUser;
import io.github.yak33.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 权限数据源：@SaCheckPermission / @SaCheckRole 注解鉴权时回调，
 * 数据来自登录时写入 TokenSession 的 LoginUser，不查库
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null) {
            return Collections.emptyList();
        }
        if (loginUser.isSuper()) {
            return List.of("*:*:*");
        }
        return loginUser.getPermissions();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return loginUser == null ? Collections.emptyList() : loginUser.getRoleKeys();
    }
}

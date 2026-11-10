package io.github.yak33.common.core.domain.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 登录用户上下文：登录成功后整体写入 Sa-Token 的 TokenSession，
 * 鉴权（StpInterface）与 /user/info 均从此处取值，避免反复查库
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;

    private String userName;

    private String nickName;

    /** 角色权限串集合，如 [super, common] */
    private List<String> roleKeys;

    /** 功能权限码集合，如 [system:user:list, system:user:add] */
    private List<String> permissions;

    public boolean isSuper() {
        return roleKeys != null && roleKeys.contains("super");
    }
}

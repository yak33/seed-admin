package io.github.yak33.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 当前用户信息（vben UserInfo 契约：userId/username/realName/avatar/roles）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class UserInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;

    private String username;

    /** 昵称，vben 语义为 realName */
    private String realName;

    private String avatar;

    /** 当前用户路由树中的第一个可见页面 */
    private String homePath;

    /** 角色 key 集合，供前端指令级权限判断（v-access:role） */
    private List<String> roles;
}

package io.github.yak33.common.core.constant;

/**
 * 通用常量
 *
 * @author ZHANGCHAO 2026/11/09
 */
public interface Constants {

    /** 超级管理员角色标识（拥有该角色的用户不受数据权限约束、可见全部菜单） */
    String SUPER_ROLE_KEY = "super";

    /** 系统内置用户不可删除/停用的管理员 userId */
    String SUPER_USER_ID = "1";
    /** 系统内置超级管理员角色 ID */
    String SUPER_ROLE_ID = "1";

    /** 通用状态：正常 */
    String STATUS_NORMAL = "0";
    /** 通用状态：停用 */
    String STATUS_DISABLE = "1";
}

package io.github.yak33.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yak33.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 角色-菜单关联（对应表 sys_role_menu）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenu extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String roleId;

    private String menuId;

    public SysRoleMenu() {
    }

    public SysRoleMenu(String roleId, String menuId) {
        this.id = UUID.randomUUID().toString();
        this.roleId = roleId;
        this.menuId = menuId;
    }
}

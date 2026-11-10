package io.github.yak33.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yak33.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 用户-角色关联（对应表 sys_user_role）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private String id;

    private String userId;

    private String roleId;

    public SysUserRole() {
    }

    public SysUserRole(String userId, String roleId) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.roleId = roleId;
    }
}

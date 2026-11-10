package io.github.yak33.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yak33.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体（对应表 sys_role）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色ID */
    @TableId(value = "id", type = IdType.INPUT)
    private String roleId;

    /** 角色名称 */
    private String roleName;

    /** 角色权限串（super 为超管标识） */
    private String roleKey;

    /** 显示顺序 */
    private Integer roleSort;

    /** 状态（0正常 1停用） */
    private String status;

    private String remark;

    @TableLogic
    private String delFlag;
}

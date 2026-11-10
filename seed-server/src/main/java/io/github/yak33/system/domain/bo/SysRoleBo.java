package io.github.yak33.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色新增/修改入参
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class SysRoleBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 为空表示新增 */
    private String roleId;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色标识不能为空")
    private String roleKey;

    private Integer roleSort;

    /** 状态（0正常 1停用） */
    private String status;

    private String remark;

    @NotEmpty(message = "角色菜单不能为空")
    private List<String> menuIds;
}

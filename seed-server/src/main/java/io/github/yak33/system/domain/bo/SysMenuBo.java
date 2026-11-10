package io.github.yak33.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单新增/修改入参
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class SysMenuBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 为空表示新增 */
    private String menuId;

    private String parentId;

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    private String routeName;

    private Integer orderNum;

    private String path;

    private String component;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    /** 显示状态（0显示 1隐藏） */
    private String visible;

    /** 菜单状态（0正常 1停用） */
    private String status;

    private String perms;

    private String icon;
}

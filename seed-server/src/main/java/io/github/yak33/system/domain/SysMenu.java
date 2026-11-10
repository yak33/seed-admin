package io.github.yak33.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yak33.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体（对应表 sys_menu）。
 * <p>path/component 语义对齐 vben 后端路由：目录存 /xxx，页面存相对路径，
 * component 存相对 src/views 的页面路径（如 /system/user/index）</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 菜单ID */
    @TableId(value = "id", type = IdType.INPUT)
    private String menuId;

    /** 菜单标题（展示用中文） */
    private String menuName;

    /** 前端路由 name（vben 依赖唯一 name 做 keep-alive 与 tab 匹配） */
    private String routeName;

    /** 父菜单ID（0 为根） */
    private String parentId;

    /** 显示顺序 */
    private Integer orderNum;

    /** 路由地址：目录为 /xxx，页面为相对父级的路径 */
    private String path;

    /** 页面组件路径：相对 src/views，如 /system/user/index；目录为空 */
    private String component;

    /** 菜单类型（M目录 C菜单 F按钮） */
    private String menuType;

    /** 显示状态（0显示 1隐藏） */
    private String visible;

    /** 菜单状态（0正常 1停用） */
    private String status;

    /** 权限标识（F 按钮必填，如 system:user:list） */
    private String perms;

    /** 图标（iconify 名称，如 lucide:settings） */
    private String icon;
}

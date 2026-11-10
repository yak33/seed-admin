package io.github.yak33.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态菜单节点（对齐 vben RouteRecordStringComponent 契约，
 * 参照官方 backend-mock 的 dashboardMenus 数据结构）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class RouterVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 路由名，全局唯一（vben 用于 keep-alive 与页签匹配） */
    private String name;

    /** 目录为 /xxx 绝对路径，页面为相对父级的路径 */
    private String path;

    /** 页面组件：相对 src/views 的路径，如 /system/user/index；目录为 null */
    private String component;

    /** 目录的重定向地址（取第一个可见子菜单的完整路径） */
    private String redirect;

    private RouterMetaVo meta;

    private List<RouterVo> children = new ArrayList<>();

    /**
     * 菜单元信息（vben meta 契约的常用子集）
     */
    @Data
    public static class RouterMetaVo implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /** 菜单标题 */
        private String title;

        /** iconify 图标 */
        private String icon;

        /** 排序，越小越靠前 */
        private Integer order;

        /** 是否在菜单中隐藏（路由仍可访问） */
        private Boolean hideInMenu;

        /** 页面是否缓存 */
        private Boolean keepAlive;
    }
}

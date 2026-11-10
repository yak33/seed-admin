package io.github.yak33.system.domain.vo;

import io.github.yak33.system.domain.SysMenu;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单管理树节点
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class SysMenuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String menuId;

    private String parentId;

    private String menuName;

    private String routeName;

    private Integer orderNum;

    private String path;

    private String component;

    private String menuType;

    private String visible;

    private String status;

    private String perms;

    private String icon;

    private List<SysMenuVo> children = new ArrayList<>();

    public static SysMenuVo from(SysMenu source) {
        SysMenuVo vo = new SysMenuVo();
        vo.setMenuId(source.getMenuId());
        vo.setParentId(source.getParentId());
        vo.setMenuName(source.getMenuName());
        vo.setRouteName(source.getRouteName());
        vo.setOrderNum(source.getOrderNum());
        vo.setPath(source.getPath());
        vo.setComponent(source.getComponent());
        vo.setMenuType(source.getMenuType());
        vo.setVisible(source.getVisible());
        vo.setStatus(source.getStatus());
        vo.setPerms(source.getPerms());
        vo.setIcon(source.getIcon());
        return vo;
    }
}

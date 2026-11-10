package io.github.yak33.system.service;

import io.github.yak33.system.domain.bo.SysMenuBo;
import io.github.yak33.system.domain.vo.RouterVo;
import io.github.yak33.system.domain.vo.SysMenuVo;

import java.util.List;

/**
 * 菜单服务
 *
 * @author ZHANGCHAO 2026/11/09
 */
public interface ISysMenuService {

    /** 查询用户可见的菜单树（vben 路由格式，不含按钮） */
    List<RouterVo> selectMenuTreeByUserId(String userId, boolean isSuper);

    /** 查询用户功能权限码集合（超管返回全部权限码） */
    List<String> selectPermsByUserId(String userId, boolean isSuper);

    /** 查询当前用户第一个可见页面地址 */
    String selectHomePathByUserId(String userId, boolean isSuper);

    /** 菜单管理树，包含停用节点和按钮 */
    List<SysMenuVo> selectMenuList();

    /** 角色授权树，停用节点及其后代不参与授权 */
    List<SysMenuVo> selectMenuTreeForRole();

    void insertMenu(SysMenuBo bo);

    void updateMenu(SysMenuBo bo);

    void deleteMenuById(String menuId);
}

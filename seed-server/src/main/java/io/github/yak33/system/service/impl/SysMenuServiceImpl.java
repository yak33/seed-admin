package io.github.yak33.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.yak33.common.core.constant.Constants;
import io.github.yak33.common.core.exception.ServiceException;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.SysMenu;
import io.github.yak33.system.domain.SysRole;
import io.github.yak33.system.domain.SysRoleMenu;
import io.github.yak33.system.domain.SysUserRole;
import io.github.yak33.system.domain.bo.SysMenuBo;
import io.github.yak33.system.domain.vo.RouterVo;
import io.github.yak33.system.domain.vo.SysMenuVo;
import io.github.yak33.system.mapper.SysMenuMapper;
import io.github.yak33.system.mapper.SysRoleMapper;
import io.github.yak33.system.mapper.SysRoleMenuMapper;
import io.github.yak33.system.mapper.SysUserRoleMapper;
import io.github.yak33.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 菜单树、动态路由及菜单权限服务实现
 *
 * @author ZHANGCHAO 2026/11/09
 */
@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl implements ISysMenuService {

    private static final String TYPE_DIRECTORY = "M";
    private static final String TYPE_MENU = "C";
    private static final String TYPE_BUTTON = "F";
    private static final String BASIC_LAYOUT = "BasicLayout";

    private final SysMenuMapper menuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public List<RouterVo> selectMenuTreeByUserId(String userId, boolean isSuper) {
        List<SysMenu> menus = isSuper ? selectAllMenus() : selectMenusByUserId(userId);
        List<SysMenu> visibleMenus = menus.stream()
            .filter(menu -> !TYPE_BUTTON.equals(menu.getMenuType()))
            .sorted(menuComparator())
            .toList();
        return buildRouterTree(visibleMenus);
    }

    @Override
    public List<String> selectPermsByUserId(String userId, boolean isSuper) {
        List<SysMenu> menus = isSuper ? selectAllMenus() : selectMenusByUserId(userId);
        return menus.stream()
            .map(SysMenu::getPerms)
            .filter(perms -> perms != null && !perms.isBlank())
            .distinct()
            .toList();
    }

    @Override
    public String selectHomePathByUserId(String userId, boolean isSuper) {
        return findHomePath(selectMenuTreeByUserId(userId, isSuper), "");
    }

    @Override
    public List<SysMenuVo> selectMenuList() {
        return buildMenuTree(menuMapper.selectList(null), false);
    }

    @Override
    public List<SysMenuVo> selectMenuTreeForRole() {
        return buildMenuTree(menuMapper.selectList(null), true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertMenu(SysMenuBo bo) {
        SysMenu menu = toEntity(bo);
        validateMenu(menu);
        menu.setMenuId(UUID.randomUUID().toString());
        menuMapper.insert(menu);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMenu(SysMenuBo bo) {
        if (bo.getMenuId() == null) {
            throw new ServiceException("修改菜单时 menuId 不能为空");
        }
        getMenu(bo.getMenuId());
        SysMenu menu = toEntity(bo);
        validateMenu(menu);
        menuMapper.updateById(menu);
        logoutAffectedUsers(bo.getMenuId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMenuById(String menuId) {
        getMenu(menuId);
        LambdaQueryWrapper<SysMenu> childWrapper = Wrappers.<SysMenu>lambdaQuery()
            .eq(SysMenu::getParentId, menuId);
        if (menuMapper.selectCount(childWrapper) > 0) {
            throw new ServiceException("存在子菜单或按钮，不能删除");
        }
        LambdaQueryWrapper<SysRoleMenu> roleWrapper = Wrappers.<SysRoleMenu>lambdaQuery()
            .eq(SysRoleMenu::getMenuId, menuId);
        if (roleMenuMapper.selectCount(roleWrapper) > 0) {
            throw new ServiceException("菜单已分配角色，请先取消授权再删除");
        }
        menuMapper.deleteById(menuId);
        logoutAffectedUsers(menuId);
    }

    private List<SysMenu> selectAllMenus() {
        LambdaQueryWrapper<SysMenu> wrapper = Wrappers.<SysMenu>lambdaQuery()
            .eq(SysMenu::getStatus, Constants.STATUS_NORMAL);
        return menuMapper.selectList(wrapper);
    }

    private List<SysMenu> selectMenusByUserId(String userId) {
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getUserId, userId);
        List<String> roleIds = userRoleMapper.selectList(userRoleWrapper).stream()
            .map(SysUserRole::getRoleId)
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SysRole> roleWrapper = Wrappers.<SysRole>lambdaQuery()
            .in(SysRole::getRoleId, roleIds)
            .eq(SysRole::getStatus, Constants.STATUS_NORMAL);
        List<String> validRoleIds = roleMapper.selectList(roleWrapper).stream()
            .map(SysRole::getRoleId)
            .toList();
        if (validRoleIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = Wrappers.<SysRoleMenu>lambdaQuery()
            .in(SysRoleMenu::getRoleId, validRoleIds);
        List<String> menuIds = roleMenuMapper.selectList(roleMenuWrapper).stream()
            .map(SysRoleMenu::getMenuId)
            .distinct()
            .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SysMenu> menuWrapper = Wrappers.<SysMenu>lambdaQuery()
            .in(SysMenu::getMenuId, menuIds)
            .eq(SysMenu::getStatus, Constants.STATUS_NORMAL);
        return menuMapper.selectList(menuWrapper);
    }

    private List<RouterVo> buildRouterTree(List<SysMenu> menus) {
        Map<String, List<SysMenu>> childrenIndex = menus.stream()
            .filter(menu -> menu.getParentId() != null && !"0".equals(menu.getParentId()))
            .collect(Collectors.groupingBy(SysMenu::getParentId));
        return menus.stream()
            .filter(menu -> menu.getParentId() == null || "0".equals(menu.getParentId()))
            .map(menu -> buildRouterNode(menu, childrenIndex))
            .toList();
    }

    private RouterVo buildRouterNode(SysMenu menu, Map<String, List<SysMenu>> childrenIndex) {
        RouterVo node = toRouterVo(menu);
        List<RouterVo> children = childrenIndex
            .getOrDefault(menu.getMenuId(), List.of()).stream()
            .map(child -> buildRouterNode(child, childrenIndex))
            .toList();
        node.setChildren(children);
        setDirectoryRedirect(node, menu, children);
        return node;
    }

    private void setDirectoryRedirect(RouterVo node, SysMenu menu, List<RouterVo> children) {
        for (RouterVo child : children) {
            boolean hidden = child.getMeta() != null && Boolean.TRUE.equals(child.getMeta().getHideInMenu());
            if (hidden) {
                continue;
            }
            String target = child.getPath().startsWith("/")
                ? child.getPath()
                : menu.getPath() + "/" + child.getPath();
            node.setRedirect(target);
            return;
        }
    }

    private RouterVo toRouterVo(SysMenu menu) {
        RouterVo vo = new RouterVo();
        vo.setName(menu.getRouteName());
        vo.setPath(menu.getPath());
        vo.setComponent(TYPE_DIRECTORY.equals(menu.getMenuType()) ? BASIC_LAYOUT : menu.getComponent());
        RouterVo.RouterMetaVo meta = new RouterVo.RouterMetaVo();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setOrder(menu.getOrderNum());
        meta.setHideInMenu(Constants.STATUS_DISABLE.equals(menu.getVisible()));
        vo.setMeta(meta);
        return vo;
    }

    private String findHomePath(List<RouterVo> nodes, String parentPath) {
        for (RouterVo node : nodes) {
            String fullPath = node.getPath().startsWith("/")
                ? node.getPath()
                : parentPath + "/" + node.getPath();
            boolean hidden = node.getMeta() != null && Boolean.TRUE.equals(node.getMeta().getHideInMenu());
            if (!BASIC_LAYOUT.equals(node.getComponent()) && !hidden) {
                return fullPath;
            }
            String childHome = findHomePath(node.getChildren(), fullPath);
            if (childHome != null) {
                return childHome;
            }
        }
        return null;
    }

    private List<SysMenuVo> buildMenuTree(List<SysMenu> menus, boolean onlyNormal) {
        Map<String, List<SysMenu>> childrenIndex = menus.stream()
            .filter(menu -> menu.getParentId() != null && !"0".equals(menu.getParentId()))
            .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenuVo> roots = new ArrayList<>();
        menus.stream()
            .filter(menu -> menu.getParentId() == null || "0".equals(menu.getParentId()))
            .sorted(menuComparator())
            .forEach(menu -> addMenuNode(menu, childrenIndex, onlyNormal, roots));
        return roots;
    }

    private void addMenuNode(SysMenu menu, Map<String, List<SysMenu>> childrenIndex,
                             boolean onlyNormal, List<SysMenuVo> target) {
        if (onlyNormal && !Constants.STATUS_NORMAL.equals(menu.getStatus())) {
            return;
        }
        SysMenuVo node = SysMenuVo.from(menu);
        List<SysMenu> children = childrenIndex.getOrDefault(menu.getMenuId(), List.of())
            .stream()
            .sorted(menuComparator())
            .toList();
        for (SysMenu child : children) {
            addMenuNode(child, childrenIndex, onlyNormal, node.getChildren());
        }
        target.add(node);
    }

    private SysMenu toEntity(SysMenuBo bo) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(bo.getMenuId());
        menu.setParentId(bo.getParentId() == null ? "0" : bo.getParentId());
        menu.setMenuName(bo.getMenuName());
        menu.setRouteName(bo.getRouteName());
        menu.setOrderNum(bo.getOrderNum() == null ? 0 : bo.getOrderNum());
        menu.setPath(bo.getPath());
        menu.setComponent(bo.getComponent());
        menu.setMenuType(bo.getMenuType());
        menu.setVisible(bo.getVisible() == null ? Constants.STATUS_NORMAL : bo.getVisible());
        menu.setStatus(bo.getStatus() == null ? Constants.STATUS_NORMAL : bo.getStatus());
        menu.setPerms(bo.getPerms());
        menu.setIcon(bo.getIcon() == null || bo.getIcon().isBlank() ? "#" : bo.getIcon());
        return menu;
    }

    private void validateMenu(SysMenu menu) {
        Map<String, SysMenu> menuMap = menuMapper.selectList(null).stream()
            .collect(Collectors.toMap(SysMenu::getMenuId, Function.identity()));
        String parentId = menu.getParentId();
        SysMenu parent = "0".equals(parentId) ? null : menuMap.get(parentId);
        if (!"0".equals(parentId) && parent == null) {
            throw new ServiceException("父菜单不存在：parentId=" + parentId);
        }
        if ("0".equals(parentId) && !TYPE_DIRECTORY.equals(menu.getMenuType())) {
            throw new ServiceException("根节点只能是目录");
        }
        if (menu.getMenuId() != null && menu.getMenuId().equals(parentId)) {
            throw new ServiceException("父菜单不能选择当前菜单自身");
        }
        if (menu.getMenuId() != null && isDescendant(parentId, menu.getMenuId(), menuMap)) {
            throw new ServiceException("父菜单不能选择当前菜单的后代节点");
        }
        switch (menu.getMenuType()) {
            case TYPE_DIRECTORY -> validateDirectory(menu, parent);
            case TYPE_MENU -> validatePage(menu, parent);
            case TYPE_BUTTON -> validateButton(menu, parent);
            default -> throw new ServiceException("未知菜单类型：" + menu.getMenuType());
        }
        checkMenuUnique(menu);
    }

    private void validateDirectory(SysMenu menu, SysMenu parent) {
        if (parent != null && !TYPE_DIRECTORY.equals(parent.getMenuType())) {
            throw new ServiceException("目录的父节点只能是目录");
        }
        if (menu.getRouteName() == null || menu.getRouteName().isBlank()) {
            throw new ServiceException("目录路由名称不能为空");
        }
        if (menu.getPath() == null || !menu.getPath().startsWith("/")) {
            throw new ServiceException("目录路由地址必须以 / 开头");
        }
        menu.setComponent(null);
    }

    private void validatePage(SysMenu menu, SysMenu parent) {
        requireParentType(parent, TYPE_DIRECTORY);
        if (menu.getRouteName() == null || menu.getRouteName().isBlank()) {
            throw new ServiceException("页面路由名称不能为空");
        }
        if (menu.getPath() == null || menu.getPath().isBlank() || menu.getPath().startsWith("/")) {
            throw new ServiceException("页面路由地址应为相对父目录的路径");
        }
        if (menu.getComponent() == null || menu.getComponent().isBlank()) {
            throw new ServiceException("页面组件路径不能为空");
        }
    }

    private void validateButton(SysMenu menu, SysMenu parent) {
        requireParentType(parent, TYPE_MENU);
        if (menu.getPerms() == null || menu.getPerms().isBlank()) {
            throw new ServiceException("按钮权限标识不能为空");
        }
        menu.setRouteName(null);
        menu.setPath("");
        menu.setComponent(null);
    }

    private void requireParentType(SysMenu parent, String expectedType) {
        if (parent == null || !expectedType.equals(parent.getMenuType())) {
            throw new ServiceException("菜单类型与父节点类型不匹配");
        }
    }

    private boolean isDescendant(String candidateParentId, String menuId, Map<String, SysMenu> menuMap) {
        String currentId = candidateParentId;
        while (currentId != null && !"0".equals(currentId)) {
            if (currentId.equals(menuId)) {
                return true;
            }
            SysMenu current = menuMap.get(currentId);
            if (current == null) {
                return false;
            }
            currentId = current.getParentId();
        }
        return false;
    }

    private void checkMenuUnique(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> nameWrapper = Wrappers.<SysMenu>lambdaQuery()
            .eq(SysMenu::getParentId, menu.getParentId())
            .eq(SysMenu::getMenuName, menu.getMenuName())
            .ne(menu.getMenuId() != null, SysMenu::getMenuId, menu.getMenuId());
        if (menuMapper.selectCount(nameWrapper) > 0) {
            throw new ServiceException("同一父菜单下名称已存在：" + menu.getMenuName());
        }
        if (menu.getRouteName() != null && !menu.getRouteName().isBlank()) {
            LambdaQueryWrapper<SysMenu> routeWrapper = Wrappers.<SysMenu>lambdaQuery()
                .eq(SysMenu::getRouteName, menu.getRouteName())
                .ne(menu.getMenuId() != null, SysMenu::getMenuId, menu.getMenuId());
            if (menuMapper.selectCount(routeWrapper) > 0) {
                throw new ServiceException("路由名称已存在：" + menu.getRouteName());
            }
        }
    }

    private SysMenu getMenu(String menuId) {
        SysMenu menu = menuMapper.selectById(menuId);
        if (menu == null) {
            throw new ServiceException("菜单不存在：menuId=" + menuId);
        }
        return menu;
    }

    private void logoutAffectedUsers(String menuId) {
        LambdaQueryWrapper<SysRoleMenu> roleMenuWrapper = Wrappers.<SysRoleMenu>lambdaQuery()
            .eq(SysRoleMenu::getMenuId, menuId);
        Set<String> roleIds = roleMenuMapper.selectList(roleMenuWrapper).stream()
            .map(SysRoleMenu::getRoleId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        LambdaQueryWrapper<SysRole> superRoleWrapper = Wrappers.<SysRole>lambdaQuery()
            .eq(SysRole::getRoleKey, Constants.SUPER_ROLE_KEY);
        roleIds.addAll(roleMapper.selectList(superRoleWrapper).stream().map(SysRole::getRoleId).toList());
        if (roleIds.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = Wrappers.<SysUserRole>lambdaQuery()
            .in(SysUserRole::getRoleId, roleIds);
        List<String> userIds = userRoleMapper.selectList(userRoleWrapper).stream()
            .map(SysUserRole::getUserId)
            .distinct()
            .toList();
        for (String userId : userIds) {
            LoginHelper.logoutByUserId(userId);
        }
    }

    private Comparator<SysMenu> menuComparator() {
        return Comparator.comparing(menu -> menu.getOrderNum() == null ? 0 : menu.getOrderNum());
    }
}

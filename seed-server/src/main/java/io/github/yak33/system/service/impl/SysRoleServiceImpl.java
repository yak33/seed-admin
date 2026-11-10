package io.github.yak33.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.yak33.common.core.constant.Constants;
import io.github.yak33.common.core.exception.ServiceException;
import io.github.yak33.common.mybatis.core.page.PageQuery;
import io.github.yak33.common.mybatis.core.page.TableDataInfo;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.SysMenu;
import io.github.yak33.system.domain.SysRole;
import io.github.yak33.system.domain.SysRoleMenu;
import io.github.yak33.system.domain.SysUserRole;
import io.github.yak33.system.domain.bo.SysRoleBo;
import io.github.yak33.system.domain.vo.SysMenuVo;
import io.github.yak33.system.domain.vo.SysRoleVo;
import io.github.yak33.system.mapper.SysMenuMapper;
import io.github.yak33.system.mapper.SysRoleMapper;
import io.github.yak33.system.mapper.SysRoleMenuMapper;
import io.github.yak33.system.mapper.SysUserRoleMapper;
import io.github.yak33.system.service.ISysMenuService;
import io.github.yak33.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 角色及角色菜单授权服务实现
 *
 * @author ZHANGCHAO 2026/11/09
 */
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuMapper menuMapper;
    private final ISysMenuService menuService;

    @Override
    public TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.<SysRole>lambdaQuery()
            .like(bo.getRoleName() != null, SysRole::getRoleName, bo.getRoleName())
            .like(bo.getRoleKey() != null, SysRole::getRoleKey, bo.getRoleKey())
            .eq(bo.getStatus() != null, SysRole::getStatus, bo.getStatus())
            .orderByAsc(SysRole::getRoleSort, SysRole::getRoleId);
        var page = roleMapper.selectPage(pageQuery.build(), wrapper);
        List<SysRoleVo> rows = page.getRecords().stream().map(SysRoleVo::from).toList();
        return TableDataInfo.build(page.getTotal(), rows);
    }

    @Override
    public SysRoleVo selectRoleById(String roleId) {
        SysRole role = getRole(roleId);
        SysRoleVo vo = SysRoleVo.from(role);
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.<SysRoleMenu>lambdaQuery()
            .eq(SysRoleMenu::getRoleId, roleId);
        List<String> menuIds = roleMenuMapper.selectList(wrapper).stream()
            .map(SysRoleMenu::getMenuId)
            .toList();
        vo.setMenuIds(menuIds);
        return vo;
    }

    @Override
    public List<SysMenuVo> selectRoleMenuTree() {
        return menuService.selectMenuTreeForRole();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertRole(SysRoleBo bo) {
        if (Constants.SUPER_ROLE_KEY.equals(bo.getRoleKey())) {
            throw new ServiceException("内置超级管理员标识不允许使用");
        }
        checkRoleUnique(bo);
        List<String> menuIds = validateAndCompleteMenuIds(bo.getMenuIds());

        SysRole role = new SysRole();
        role.setRoleName(bo.getRoleName());
        role.setRoleKey(bo.getRoleKey());
        role.setRoleSort(bo.getRoleSort() == null ? 0 : bo.getRoleSort());
        role.setStatus(bo.getStatus() == null ? Constants.STATUS_NORMAL : bo.getStatus());
        role.setRemark(bo.getRemark());
        role.setRoleId(UUID.randomUUID().toString());
        roleMapper.insert(role);
        saveRoleMenus(role.getRoleId(), menuIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRole(SysRoleBo bo) {
        if (bo.getRoleId() == null) {
            throw new ServiceException("修改角色时 roleId 不能为空");
        }
        SysRole existing = getRole(bo.getRoleId());
        checkRoleAllowed(existing);
        if (Constants.SUPER_ROLE_KEY.equals(bo.getRoleKey())) {
            throw new ServiceException("普通角色不能修改为超级管理员标识");
        }
        checkRoleUnique(bo);
        if (Constants.STATUS_DISABLE.equals(bo.getStatus()) && countRoleUsers(bo.getRoleId()) > 0) {
            throw new ServiceException("角色已分配用户，不能停用：" + existing.getRoleName());
        }
        List<String> menuIds = validateAndCompleteMenuIds(bo.getMenuIds());

        SysRole role = new SysRole();
        role.setRoleId(bo.getRoleId());
        role.setRoleName(bo.getRoleName());
        role.setRoleKey(bo.getRoleKey());
        role.setRoleSort(bo.getRoleSort());
        role.setStatus(bo.getStatus());
        role.setRemark(bo.getRemark());
        roleMapper.updateById(role);
        saveRoleMenus(bo.getRoleId(), menuIds);
        logoutRoleUsers(bo.getRoleId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRoleByIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ServiceException("请选择要删除的角色");
        }
        for (String roleId : roleIds) {
            SysRole role = getRole(roleId);
            checkRoleAllowed(role);
            if (countRoleUsers(roleId) > 0) {
                throw new ServiceException("角色已分配用户，不能删除：" + role.getRoleName());
            }
        }
        roleMapper.deleteByIds(roleIds);
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.<SysRoleMenu>lambdaQuery()
            .in(SysRoleMenu::getRoleId, roleIds);
        roleMenuMapper.delete(wrapper);
    }

    private SysRole getRole(String roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在或已被删除：roleId=" + roleId);
        }
        return role;
    }

    private void checkRoleAllowed(SysRole role) {
        if (Constants.SUPER_ROLE_ID.equals(role.getRoleId())
            || Constants.SUPER_ROLE_KEY.equals(role.getRoleKey())) {
            throw new ServiceException("内置超级管理员角色不允许修改或删除");
        }
    }

    private void checkRoleUnique(SysRoleBo bo) {
        LambdaQueryWrapper<SysRole> nameWrapper = Wrappers.<SysRole>lambdaQuery()
            .eq(SysRole::getRoleName, bo.getRoleName())
            .ne(bo.getRoleId() != null, SysRole::getRoleId, bo.getRoleId());
        if (roleMapper.selectCount(nameWrapper) > 0) {
            throw new ServiceException("角色名称已存在：" + bo.getRoleName());
        }
        LambdaQueryWrapper<SysRole> keyWrapper = Wrappers.<SysRole>lambdaQuery()
            .eq(SysRole::getRoleKey, bo.getRoleKey())
            .ne(bo.getRoleId() != null, SysRole::getRoleId, bo.getRoleId());
        if (roleMapper.selectCount(keyWrapper) > 0) {
            throw new ServiceException("角色标识已存在：" + bo.getRoleKey());
        }
    }

    private long countRoleUsers(String roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getRoleId, roleId);
        return userRoleMapper.selectCount(wrapper);
    }

    private List<String> validateAndCompleteMenuIds(List<String> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            throw new ServiceException("角色至少需要一个菜单权限");
        }
        Set<String> selectedIds = new LinkedHashSet<>(menuIds);
        List<SysMenu> selectedMenus = menuMapper.selectBatchIds(selectedIds);
        if (selectedMenus.size() != selectedIds.size()) {
            throw new ServiceException("部分菜单不存在");
        }
        for (SysMenu menu : selectedMenus) {
            if (!Constants.STATUS_NORMAL.equals(menu.getStatus())) {
                throw new ServiceException("菜单已停用，不能授权：" + menu.getMenuName());
            }
        }

        Map<String, SysMenu> allMenuMap = menuMapper.selectList(null).stream()
            .collect(Collectors.toMap(SysMenu::getMenuId, Function.identity()));
        Set<String> completedIds = new LinkedHashSet<>();
        for (String menuId : selectedIds) {
            String currentId = menuId;
            while (currentId != null && !"0".equals(currentId)) {
                SysMenu current = allMenuMap.get(currentId);
                if (current == null) {
                    throw new ServiceException("菜单的父节点不存在：menuId=" + currentId);
                }
                completedIds.add(currentId);
                currentId = current.getParentId();
            }
        }
        return List.copyOf(completedIds);
    }

    private void saveRoleMenus(String roleId, List<String> menuIds) {
        LambdaQueryWrapper<SysRoleMenu> wrapper = Wrappers.<SysRoleMenu>lambdaQuery()
            .eq(SysRoleMenu::getRoleId, roleId);
        roleMenuMapper.delete(wrapper);
        for (String menuId : menuIds) {
            roleMenuMapper.insert(new SysRoleMenu(roleId, menuId));
        }
    }

    private void logoutRoleUsers(String roleId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getRoleId, roleId);
        List<String> userIds = userRoleMapper.selectList(wrapper).stream()
            .map(SysUserRole::getUserId)
            .distinct()
            .toList();
        for (String userId : userIds) {
            LoginHelper.logoutByUserId(userId);
        }
    }
}

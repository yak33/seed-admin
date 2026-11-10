package io.github.yak33.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.yak33.common.core.domain.R;
import io.github.yak33.common.mybatis.core.page.PageQuery;
import io.github.yak33.common.mybatis.core.page.TableDataInfo;
import io.github.yak33.system.domain.bo.SysRoleBo;
import io.github.yak33.system.domain.vo.SysMenuVo;
import io.github.yak33.system.domain.vo.SysRoleVo;
import io.github.yak33.system.service.ISysRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色及角色菜单授权接口
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {

    private final ISysRoleService roleService;

    @SaCheckPermission("system:role:list")
    @GetMapping("/list")
    public TableDataInfo<SysRoleVo> list(SysRoleBo bo, @Valid PageQuery pageQuery) {
        return roleService.selectPageRoleList(bo, pageQuery);
    }

    @SaCheckPermission("system:role:list")
    @GetMapping("/menu/tree")
    public R<List<SysMenuVo>> menuTree() {
        return R.ok(roleService.selectRoleMenuTree());
    }

    @SaCheckPermission("system:role:list")
    @GetMapping("/{roleId}")
    public R<SysRoleVo> getInfo(@PathVariable String roleId) {
        return R.ok(roleService.selectRoleById(roleId));
    }

    @SaCheckPermission("system:role:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysRoleBo bo) {
        roleService.insertRole(bo);
        return R.ok();
    }

    @SaCheckPermission("system:role:edit")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysRoleBo bo) {
        roleService.updateRole(bo);
        return R.ok();
    }

    @SaCheckPermission("system:role:remove")
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable List<String> roleIds) {
        roleService.deleteRoleByIds(roleIds);
        return R.ok();
    }
}

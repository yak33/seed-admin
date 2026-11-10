package io.github.yak33.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.github.yak33.common.core.domain.R;
import io.github.yak33.common.core.domain.model.LoginUser;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.bo.SysMenuBo;
import io.github.yak33.system.domain.vo.RouterVo;
import io.github.yak33.system.domain.vo.SysMenuVo;
import io.github.yak33.system.service.ISysMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * 动态路由与菜单管理接口
 *
 * @author ZHANGCHAO 2026/11/09
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SysMenuController {

    private final ISysMenuService menuService;

    @SaCheckLogin
    @GetMapping("/menu/all")
    public R<List<RouterVo>> getAllMenus() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RouterVo> menus = menuService.selectMenuTreeByUserId(
            loginUser.getUserId(), loginUser.isSuper());
        return R.ok(menus);
    }

    @SaCheckPermission("system:menu:list")
    @GetMapping("/system/menu/list")
    public R<List<SysMenuVo>> list() {
        return R.ok(menuService.selectMenuList());
    }

    @SaCheckRole("super")
    @SaCheckPermission("system:menu:add")
    @PostMapping("/system/menu")
    public R<Void> add(@Valid @RequestBody SysMenuBo bo) {
        menuService.insertMenu(bo);
        return R.ok();
    }

    @SaCheckRole("super")
    @SaCheckPermission("system:menu:edit")
    @PutMapping("/system/menu")
    public R<Void> edit(@Valid @RequestBody SysMenuBo bo) {
        menuService.updateMenu(bo);
        return R.ok();
    }

    @SaCheckRole("super")
    @SaCheckPermission("system:menu:remove")
    @DeleteMapping("/system/menu/{menuId}")
    public R<Void> remove(@PathVariable String menuId) {
        menuService.deleteMenuById(menuId);
        return R.ok();
    }
}

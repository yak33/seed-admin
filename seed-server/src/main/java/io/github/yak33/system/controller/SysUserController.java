package io.github.yak33.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.yak33.common.core.domain.R;
import io.github.yak33.common.mybatis.core.page.PageQuery;
import io.github.yak33.common.mybatis.core.page.TableDataInfo;
import io.github.yak33.system.domain.bo.SysUserBo;
import io.github.yak33.system.domain.vo.SysRoleVo;
import io.github.yak33.system.domain.vo.SysUserVo;
import io.github.yak33.system.service.ISysUserService;
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
 * 用户管理接口（示例 CRUD，新业务模块照此分层复制）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {

    private final ISysUserService userService;

    /**
     * 分页查询用户列表
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/list")
    public TableDataInfo<SysUserVo> list(SysUserBo bo, @Valid PageQuery pageQuery) {
        return userService.selectPageUserList(bo, pageQuery);
    }

    /**
     * 可分配角色选项，内置超管角色不下发
     */
    @SaCheckPermission("system:user:list")
    @GetMapping("/role/options")
    public R<List<SysRoleVo>> roleOptions() {
        return R.ok(userService.selectRoleOptions());
    }

    /**
     * 新增用户（未填密码时默认 123456）
     */
    @SaCheckPermission("system:user:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysUserBo bo) {
        userService.insertUser(bo);
        return R.ok();
    }

    /**
     * 修改用户（密码留空表示不修改）
     */
    @SaCheckPermission("system:user:edit")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody SysUserBo bo) {
        userService.updateUser(bo);
        return R.ok();
    }

    /**
     * 删除用户（批量，超管账号不允许删除）
     */
    @SaCheckPermission("system:user:remove")
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable List<String> userIds) {
        userService.deleteUserByIds(userIds);
        return R.ok();
    }
}

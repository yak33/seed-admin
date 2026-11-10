package io.github.yak33.system.service;

import io.github.yak33.common.mybatis.core.page.PageQuery;
import io.github.yak33.common.mybatis.core.page.TableDataInfo;
import io.github.yak33.system.domain.bo.SysRoleBo;
import io.github.yak33.system.domain.vo.SysMenuVo;
import io.github.yak33.system.domain.vo.SysRoleVo;

import java.util.List;

/**
 * 角色服务
 *
 * @author ZHANGCHAO 2026/11/09
 */
public interface ISysRoleService {

    TableDataInfo<SysRoleVo> selectPageRoleList(SysRoleBo bo, PageQuery pageQuery);

    SysRoleVo selectRoleById(String roleId);

    List<SysMenuVo> selectRoleMenuTree();

    void insertRole(SysRoleBo bo);

    void updateRole(SysRoleBo bo);

    void deleteRoleByIds(List<String> roleIds);
}

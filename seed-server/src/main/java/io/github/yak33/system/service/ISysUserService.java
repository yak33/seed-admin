package io.github.yak33.system.service;

import io.github.yak33.common.mybatis.core.page.PageQuery;
import io.github.yak33.common.mybatis.core.page.TableDataInfo;
import io.github.yak33.system.domain.SysUser;
import io.github.yak33.system.domain.bo.SysUserBo;
import io.github.yak33.system.domain.vo.SysRoleVo;
import io.github.yak33.system.domain.vo.SysUserVo;

import java.util.List;

/**
 * 用户服务
 *
 * @author ZHANGCHAO 2026/11/09
 */
public interface ISysUserService {

    /** 按登录账号查询（含状态/密码，供登录校验使用） */
    SysUser selectUserByUserName(String userName);

    /** 分页查询用户列表并批量装配角色 */
    TableDataInfo<SysUserVo> selectPageUserList(SysUserBo bo, PageQuery pageQuery);

    /** 查询用户角色 key 集合 */
    List<String> selectRoleKeysByUserId(String userId);

    /** 查询可分配给普通用户的角色，内置超管角色不下发 */
    List<SysRoleVo> selectRoleOptions();

    /** 新增用户并保存角色关联 */
    void insertUser(SysUserBo bo);

    /** 修改用户并重写角色关联，成功后注销旧会话 */
    void updateUser(SysUserBo bo);

    /** 批量逻辑删除用户并清理角色关联 */
    void deleteUserByIds(List<String> userIds);
}

package io.github.yak33.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.yak33.common.core.constant.Constants;
import io.github.yak33.common.core.exception.ServiceException;
import io.github.yak33.common.mybatis.core.page.PageQuery;
import io.github.yak33.common.mybatis.core.page.TableDataInfo;
import io.github.yak33.common.satoken.utils.LoginHelper;
import io.github.yak33.system.domain.SysRole;
import io.github.yak33.system.domain.SysUser;
import io.github.yak33.system.domain.SysUserRole;
import io.github.yak33.system.domain.bo.SysUserBo;
import io.github.yak33.system.domain.vo.SysRoleVo;
import io.github.yak33.system.domain.vo.SysUserVo;
import io.github.yak33.system.mapper.SysRoleMapper;
import io.github.yak33.system.mapper.SysUserMapper;
import io.github.yak33.system.mapper.SysUserRoleMapper;
import io.github.yak33.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author ZHANGCHAO 2026/11/09
 */
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SysUser selectUserByUserName(String userName) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
            .eq(SysUser::getUserName, userName);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public TableDataInfo<SysUserVo> selectPageUserList(SysUserBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
            .like(bo.getUserName() != null, SysUser::getUserName, bo.getUserName())
            .like(bo.getPhonenumber() != null, SysUser::getPhonenumber, bo.getPhonenumber())
            .eq(bo.getStatus() != null, SysUser::getStatus, bo.getStatus())
            .orderByDesc(SysUser::getUserId);

        var page = userMapper.selectPage(pageQuery.build(), wrapper);
        List<SysUserVo> rows = page.getRecords().stream().map(SysUserVo::from).toList();
        fillRoleInfo(rows);
        return TableDataInfo.build(page.getTotal(), rows);
    }

    @Override
    public List<String> selectRoleKeysByUserId(String userId) {
        List<String> roleIds = selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.<SysRole>lambdaQuery()
            .in(SysRole::getRoleId, roleIds)
            .eq(SysRole::getStatus, Constants.STATUS_NORMAL);
        return roleMapper.selectList(wrapper).stream().map(SysRole::getRoleKey).toList();
    }

    @Override
    public List<SysRoleVo> selectRoleOptions() {
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.<SysRole>lambdaQuery()
            .eq(SysRole::getStatus, Constants.STATUS_NORMAL)
            .ne(SysRole::getRoleKey, Constants.SUPER_ROLE_KEY)
            .orderByAsc(SysRole::getRoleSort, SysRole::getRoleId);
        return roleMapper.selectList(wrapper).stream().map(SysRoleVo::from).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void insertUser(SysUserBo bo) {
        checkUserNameUnique(bo);
        List<String> roleIds = validateAssignableRoles(bo.getRoleIds());

        SysUser user = new SysUser();
        user.setUserName(bo.getUserName());
        user.setNickName(bo.getNickName());
        user.setEmail(bo.getEmail());
        user.setPhonenumber(bo.getPhonenumber());
        user.setSex(bo.getSex());
        user.setStatus(bo.getStatus() == null ? Constants.STATUS_NORMAL : bo.getStatus());
        user.setRemark(bo.getRemark());
        String rawPassword = (bo.getPassword() == null || bo.getPassword().isBlank())
            ? DEFAULT_PASSWORD : bo.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setUserId(UUID.randomUUID().toString());
        userMapper.insert(user);
        saveUserRoles(user.getUserId(), roleIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateUser(SysUserBo bo) {
        if (bo.getUserId() == null) {
            throw new ServiceException("修改用户时 userId 不能为空");
        }
        SysUser existing = userMapper.selectById(bo.getUserId());
        if (existing == null) {
            throw new ServiceException("用户不存在或已被删除：userId=" + bo.getUserId());
        }
        checkUserOperationAllowed(existing.getUserId());
        checkUserNameUnique(bo);
        List<String> roleIds = validateAssignableRoles(bo.getRoleIds());

        SysUser user = new SysUser();
        user.setUserId(bo.getUserId());
        user.setNickName(bo.getNickName());
        user.setEmail(bo.getEmail());
        user.setPhonenumber(bo.getPhonenumber());
        user.setSex(bo.getSex());
        user.setStatus(bo.getStatus());
        user.setRemark(bo.getRemark());
        if (bo.getPassword() != null && !bo.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(bo.getPassword()));
        }
        userMapper.updateById(user);
        saveUserRoles(bo.getUserId(), roleIds);
        LoginHelper.logoutByUserId(bo.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUserByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new ServiceException("请选择要删除的用户");
        }
        String currentUserId = LoginHelper.getUserId();
        if (userIds.contains(currentUserId)) {
            throw new ServiceException("当前登录用户不允许删除自己");
        }
        for (String userId : userIds) {
            checkUserOperationAllowed(userId);
        }

        userMapper.deleteByIds(userIds);
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.<SysUserRole>lambdaQuery()
            .in(SysUserRole::getUserId, userIds);
        userRoleMapper.delete(wrapper);
        for (String userId : userIds) {
            LoginHelper.logoutByUserId(userId);
        }
    }

    private List<String> selectRoleIdsByUserId(String userId) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getUserId, userId);
        return userRoleMapper.selectList(wrapper).stream().map(SysUserRole::getRoleId).toList();
    }

    private void checkUserNameUnique(SysUserBo bo) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
            .eq(SysUser::getUserName, bo.getUserName())
            .ne(bo.getUserId() != null, SysUser::getUserId, bo.getUserId());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("登录账号已存在：" + bo.getUserName());
        }
    }

    private void checkUserOperationAllowed(String userId) {
        if (Constants.SUPER_USER_ID.equals(userId)) {
            throw new ServiceException("内置超级管理员不允许通过管理接口修改或删除");
        }
        List<String> roleIds = selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty() || LoginHelper.getLoginUser().isSuper()) {
            return;
        }
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        boolean targetIsSuper = roles.stream()
            .anyMatch(role -> Constants.SUPER_ROLE_KEY.equals(role.getRoleKey()));
        if (targetIsSuper) {
            throw new ServiceException("无权操作持有超级管理员角色的用户");
        }
    }

    private List<String> validateAssignableRoles(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new ServiceException("用户至少需要一个角色");
        }
        Set<String> distinctRoleIds = new LinkedHashSet<>(roleIds);
        List<SysRole> roles = roleMapper.selectBatchIds(distinctRoleIds);
        if (roles.size() != distinctRoleIds.size()) {
            throw new ServiceException("部分角色不存在或已被删除");
        }
        for (SysRole role : roles) {
            if (Constants.SUPER_ROLE_KEY.equals(role.getRoleKey())) {
                throw new ServiceException("内置超级管理员角色不允许分配");
            }
            if (!Constants.STATUS_NORMAL.equals(role.getStatus())) {
                throw new ServiceException("角色已停用，不能分配：" + role.getRoleName());
            }
        }
        return List.copyOf(distinctRoleIds);
    }

    private void saveUserRoles(String userId, List<String> roleIds) {
        LambdaQueryWrapper<SysUserRole> wrapper = Wrappers.<SysUserRole>lambdaQuery()
            .eq(SysUserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);
        for (String roleId : roleIds) {
            userRoleMapper.insert(new SysUserRole(userId, roleId));
        }
    }

    private void fillRoleInfo(List<SysUserVo> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<String> userIds = rows.stream().map(SysUserVo::getUserId).toList();
        LambdaQueryWrapper<SysUserRole> relationWrapper = Wrappers.<SysUserRole>lambdaQuery()
            .in(SysUserRole::getUserId, userIds);
        List<SysUserRole> relations = userRoleMapper.selectList(relationWrapper);
        if (relations.isEmpty()) {
            for (SysUserVo row : rows) {
                row.setRoleIds(List.of());
                row.setRoleNames(List.of());
            }
            return;
        }

        Set<String> roleIds = relations.stream().map(SysUserRole::getRoleId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, SysRole> roleMap = roleMapper.selectBatchIds(roleIds).stream()
            .collect(Collectors.toMap(SysRole::getRoleId, Function.identity()));
        Map<String, List<SysUserRole>> relationMap = relations.stream()
            .collect(Collectors.groupingBy(SysUserRole::getUserId));

        for (SysUserVo row : rows) {
            List<SysUserRole> userRelations = relationMap.getOrDefault(row.getUserId(), List.of());
            List<String> userRoleIds = new ArrayList<>();
            List<String> userRoleNames = new ArrayList<>();
            for (SysUserRole relation : userRelations) {
                userRoleIds.add(relation.getRoleId());
                SysRole role = roleMap.get(relation.getRoleId());
                if (role != null) {
                    userRoleNames.add(role.getRoleName());
                }
            }
            row.setRoleIds(userRoleIds);
            row.setRoleNames(userRoleNames);
        }
    }
}

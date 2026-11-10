package io.github.yak33.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yak33.system.domain.SysUser;

/**
 * 用户 Mapper（单表查询全部走 LambdaQueryWrapper，无 XML）
 *
 * @author ZHANGCHAO 2026/11/09
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
}

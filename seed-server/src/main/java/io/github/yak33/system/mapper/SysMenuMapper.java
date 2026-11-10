package io.github.yak33.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yak33.system.domain.SysMenu;

/**
 * 菜单 Mapper（用户-菜单关联查询在 Service 层分步查询，不写 join XML）
 *
 * @author ZHANGCHAO 2026/11/09
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {
}

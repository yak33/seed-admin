package io.github.yak33.system.domain.vo;

import io.github.yak33.system.domain.SysRole;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色列表/详情出参
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class SysRoleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String roleId;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private List<String> menuIds = List.of();

    public static SysRoleVo from(SysRole source) {
        SysRoleVo vo = new SysRoleVo();
        vo.setRoleId(source.getRoleId());
        vo.setRoleName(source.getRoleName());
        vo.setRoleKey(source.getRoleKey());
        vo.setRoleSort(source.getRoleSort());
        vo.setStatus(source.getStatus());
        vo.setRemark(source.getRemark());
        vo.setCreateTime(source.getCreateTime());
        return vo;
    }
}

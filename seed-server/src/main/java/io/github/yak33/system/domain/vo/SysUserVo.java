package io.github.yak33.system.domain.vo;

import io.github.yak33.system.domain.SysUser;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表/详情出参（不含密码）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class SysUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;

    private String userName;

    private String nickName;

    private String email;

    private String phonenumber;

    private String sex;

    private String status;

    private LocalDateTime createTime;

    private String remark;

    private List<String> roleIds;

    private List<String> roleNames;

    public static SysUserVo from(SysUser source) {
        SysUserVo vo = new SysUserVo();
        vo.setUserId(source.getUserId());
        vo.setUserName(source.getUserName());
        vo.setNickName(source.getNickName());
        vo.setEmail(source.getEmail());
        vo.setPhonenumber(source.getPhonenumber());
        vo.setSex(source.getSex());
        vo.setStatus(source.getStatus());
        vo.setCreateTime(source.getCreateTime());
        vo.setRemark(source.getRemark());
        return vo;
    }
}

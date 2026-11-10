package io.github.yak33.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户新增/修改入参（修改时必须携带 userId）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class SysUserBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 为空表示新增 */
    private String userId;

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 3, max = 30, message = "账号长度必须在 3 到 30 之间")
    private String userName;

    @NotBlank(message = "用户昵称不能为空")
    private String nickName;

    /** 新增时可为空，默认 123456；修改时为空表示不改密码 */
    private String password;

    @Size(max = 50, message = "邮箱长度不能超过 50")
    private String email;

    @Size(max = 11, message = "手机号长度不能超过 11")
    private String phonenumber;

    /** 性别（0男 1女 2未知） */
    private String sex;

    /** 状态（0正常 1停用） */
    private String status;

    private String remark;

    @NotEmpty(message = "用户角色不能为空")
    private List<String> roleIds;
}

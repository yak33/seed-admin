package io.github.yak33.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录入参（vben 登录表单只提交账号密码，验证码为前端滑块自校验）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class LoginBody implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}

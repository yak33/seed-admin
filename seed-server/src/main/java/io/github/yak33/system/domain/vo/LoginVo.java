package io.github.yak33.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应（vben 契约：只要求 accessToken 字段）
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Data
public class LoginVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 访问令牌，前端以 Bearer 头携带 */
    private String accessToken;

    /** 有效期（秒） */
    private Long expireIn;
}

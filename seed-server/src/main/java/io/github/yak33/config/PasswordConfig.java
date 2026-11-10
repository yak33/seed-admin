package io.github.yak33.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器：仅引入 spring-security-crypto 做 BCrypt 哈希，
 * 不引入 Spring Security 过滤器链
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

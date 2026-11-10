package io.github.yak33.common.redis.config;

import cn.dev33.satoken.dao.SaTokenDaoForRedisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 会话存储注册：官方 Redisson 集成（StringCodec 直存字符串，
 * 由 Sa-Token 全局 JSON 模板负责对象与字符串的互转，写读同一条链路）。
 * <p>注意：官方要求 RedisTemplate 与 Redisson 两套 Dao 只能二选一，本骨架只用 Redisson Dao</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Configuration
public class RedissonSaTokenDaoConfig {

    @Bean
    public SaTokenDaoForRedisson saTokenDao(RedissonClient redissonClient) {
        return new SaTokenDaoForRedisson(redissonClient);
    }
}

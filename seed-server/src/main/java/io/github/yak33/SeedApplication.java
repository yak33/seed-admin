package io.github.yak33;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * seed 骨架启动类。
 * <p>包扫描默认覆盖 io.github.yak33 下所有模块（含 common-* 的 @Configuration/@Component）</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@SpringBootApplication
// 新增业务模块（如 io.github.yak33.xxx.mapper）时在此追加包名
@MapperScan("io.github.yak33.system.mapper")
public class SeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeedApplication.class, args);
    }
}

package io.github.yak33.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 3（Boot 4 默认）全局 LocalDateTime 格式：
 * 旧版 spring.jackson.date-format 只对 java.util.Date 生效，
 * LocalDateTime 默认走 ISO（2026-11-09T15:40:32），这里统一为 yyyy-MM-dd HH:mm:ss
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public JsonMapperBuilderCustomizer localDateTimeFormatCustomizer() {
        return builder -> builder.addModule(new SimpleModule()
            .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER))
            .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER)));
    }
}

package io.github.yak33.common.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.util.List;

/**
 * SPA 静态资源与前端路由回退。
 * <p>前端产物打包在 classpath:/static/，history 路由刷新时 URL（如 /system/user）
 * 无对应物理文件，回退到 index.html 交给前端路由；带扩展名未命中保持 404，
 * 避免把 API 或资源请求误答成 HTML</p>
 *
 * @author ZHANGCHAO 2026/11/09
 */
@Configuration
public class SpaForwardConfig implements WebMvcConfigurer {

    private static final String[] STATIC_LOCATIONS = { "classpath:/static/" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations(STATIC_LOCATIONS)
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(String resourcePath, Resource location)
                        throws IOException {
                    Resource requested = location.createRelative(resourcePath);
                    // 真实文件（构建产物带 hash 文件名）直接返回
                    if (requested.isReadable()) {
                        return requested;
                    }
                    // 无扩展名视为前端路由路径，回退入口页面；其余保持 404
                    if (looksLikeFrontendRoute(resourcePath)) {
                        return new ClassPathResource("static/index.html");
                    }
                    return null;
                }
            });
    }

    /**
     * 最后一段不含点号即视为前端路由（/system/user、/dashboard/workspace），
     * /assets/index-abc123.js 这类资源路径不在此列
     */
    private boolean looksLikeFrontendRoute(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return true;
        }
        String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        return !lastSegment.contains(".");
    }
}

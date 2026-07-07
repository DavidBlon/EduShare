package com.wb.learningresourcelibrary.common.config;

import com.wb.learningresourcelibrary.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 注册 JWT 过滤器
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(jwtAuthenticationFilter);
        registration.addUrlPatterns("/api/admin/*");
        registration.setOrder(1);
        return registration;
    }

    /**
     * 静态资源映射（封面图片访问）
     * 映射 /uploads/** 到 file.upload-path，保证上传的图片能通过 URL 访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadPath.endsWith("/") || uploadPath.endsWith("\\") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}

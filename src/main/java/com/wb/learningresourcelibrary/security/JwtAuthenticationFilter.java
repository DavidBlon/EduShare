package com.wb.learningresourcelibrary.security;

import com.wb.learningresourcelibrary.common.constant.Constants;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 拦截 /api/admin/** 请求，验证令牌有效性
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 只拦截 /api/admin 开头的路径
        if (!requestURI.startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 放行登录接口
        if (requestURI.equals("/api/admin/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            log.warn("JWT认证失败: 缺少或无效的Authorization头, URI={}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或令牌已过期\"}");
            return;
        }

        String token = authHeader.substring(Constants.TOKEN_PREFIX.length()).trim();
        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT认证失败: 令牌无效或已过期, URI={}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"令牌无效或已过期\"}");
            return;
        }

        // 将管理员信息写入请求属性
        try {
            Claims claims = jwtUtil.parseToken(token);
            request.setAttribute(Constants.ADMIN_ID_ATTR, Long.parseLong(claims.getSubject()));
            request.setAttribute(Constants.ADMIN_USERNAME_ATTR, claims.get("username"));
            Integer role = claims.get("role", Integer.class);
            request.setAttribute(Constants.ADMIN_ROLE_ATTR, role != null ? role : Constants.ROLE_NORMAL);
        } catch (Exception e) {
            log.error("JWT解析失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"令牌解析失败\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

package com.springboot.tiku.security;

import com.springboot.tiku.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    @Value("${jwt.header}")
    private String header;
    
    @Value("${jwt.prefix}")
    private String prefix;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 跳过静态资源、健康检查等不需要JWT认证的路径
        return path.startsWith("/assets/") || 
               path.startsWith("/actuator/") ||
               path.equals("/") ||
               path.equals("/index.html") ||
               path.equals("/vite.svg") ||
               path.equals("/favicon.ico");
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 获取Token
        String token = getTokenFromRequest(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 从Token中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (username != null) {
                    // 加载用户详情
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 验证Token
                    if (jwtUtil.validateToken(token, username)) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置到SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("设置用户认证信息：{}", username);
                    }
                }
            } catch (Exception e) {
                log.error("设置用户认证失败", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(header);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(prefix.length()).trim();
        }
        return null;
    }
}





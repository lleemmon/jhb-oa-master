package com.jhb.security.filter;

import com.alibaba.fastjson.JSON;
import com.jhb.common.jwt.JwtHelper;
import com.jhb.common.result.Result;
import com.jhb.common.result.ResultCodeEnum;
import com.jhb.common.utils.ResponseUtil;
import com.jhb.security.custom.LoginUserInfoHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//认证解析过滤器
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 如果是登录接口， 直接放行
        if("/admin/system/index/login".equals(request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if(null != authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } else {
            ResponseUtil.out(response, Result.build(null, ResultCodeEnum.PERMISSION));
        }

    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader("token");
        if(token != null && !token.isEmpty()){
            String username = JwtHelper.getUsername(token);
            if(username != null && !username.isEmpty()){
                LoginUserInfoHelper.setUsername(username);
                LoginUserInfoHelper.setUserId(JwtHelper.getUserId(token));
                String authStr = (String)redisTemplate.opsForValue().get(TokenLoginFilter.USERNAME_AUTH_KEY + username);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if(authStr != null && !token.isEmpty()){
                    List<Map>maps = JSON.parseArray(authStr, Map.class);
                    authorities = maps.stream().map(auth -> new SimpleGrantedAuthority((String)auth.get("authority"))).collect(Collectors.toList());
                }
                return new UsernamePasswordAuthenticationToken(username, null, authorities);
            }
        }
        return null;
    }
}

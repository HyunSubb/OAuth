package com.example.oauth.common.auth;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

// JWT 검증 처리
@Component
public class JwtTokenFilter extends GenericFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response
            , FilterChain chain) throws IOException, ServletException {

        chain.doFilter(request, response);
    }
}

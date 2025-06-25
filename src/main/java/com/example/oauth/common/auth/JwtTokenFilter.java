package com.example.oauth.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// JWT 검증 처리
@Component
public class JwtTokenFilter extends GenericFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response
            , FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        String token = httpServletRequest.getHeader("Authorization");

        try {
            if (token != null) {
                // 토큰을 보낼 때 토큰에 "Bearer " 를 붙이고 보내는 것은 관례이다.
                if(!token.substring(0,7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }
                String jwtToken = token.substring(7);

                // token 검증 및 claims(payload) 추출
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getBody(); // getBody()를 하겠다는 거는 검증을 하고 페이로드를 꺼내겠다는 거임.
                // 꺼낸 이유는 얘를 가지고 Authentication 인증 객체에 넣어줄 거기 때문이다.

                // Authentication 객체 생성
                // 패스워드는 우리가 그냥 비워둔거다. 딱히 쓸일이 없기 때문이다.
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));
                UserDetails userDetails = new User(claims.getSubject(),"",authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, jwtToken, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 해당 필터 끝나고 다음 필터로 이동
            chain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("invalid token");
        }
    }
}

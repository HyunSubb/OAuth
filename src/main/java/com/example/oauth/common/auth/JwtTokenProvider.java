package com.example.oauth.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

// JWT 발급 처리
@Component
public class JwtTokenProvider {

    private final String secretKey;

    private final int expiration;

    private Key SECRET_KEY;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, @Value("${jwt.expiration}") int expiration) {
        this.secretKey = secretKey;
        this.expiration = expiration;
        // 시크릿키는 yml 파일에서 인코딩 된 채로 일단 들어가 있음. 인코딩된 시크릿키를 디코딩 해준 다음 암호화.
        // JWT 세번째 파트 부분에 들어갈 시크릿키를 암호화해주는 코드임.
        this.SECRET_KEY = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKey), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createToken(String email, String role) {
        // Claims는 jwt 토큰의 payload 부분을 의미한다.
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L))
                // signWith는 시그니처 부분임. 시크릿키를 넣어주자.
                .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                .compact();

        return token;
    }
}

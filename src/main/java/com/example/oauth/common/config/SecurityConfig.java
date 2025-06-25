package com.example.oauth.common.config;

import com.example.oauth.common.auth.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    // 패스워드 암호화를 위한 빈을 생성함.
    @Bean
    public PasswordEncoder makePassword() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain myfilter(HttpSecurity httpSecurity) throws Exception{

        return httpSecurity
                // cors는 프런트 화면이 있을 때 의미가 있는 설정인데 같은 도메인 끼리만 api를 통해서 데이터를 주고받겠다라는 설정이다.
                // 우린 서버는 8080 프런트는 3000 으로 설정할 거임. 두 개가 서로 도메인이 다름. 그래서 3000번 같은 경우에는 허용하겠다고 설정을 해줘야 함.
                .cors(cors -> cors.configurationSource(configurationSource()))
                // csrf 비활성화
                // csrf 라고 하는 것은 보안 공격 중 하나인데 보통 mvc 패턴에서 많이 사용되는 공격이다.
                // 우리는 mvc 패턴이 아니고 restful 설계가 되어 있고, 프런트가 별도로 있는 구조라서 csrf는 따로 설정하지 않겠다.
                .csrf(AbstractHttpConfigurer::disable)
                // Basic 인증 비활성화 -> 사용자 이름과 비밀번호를 Base64로 인코딩 하여 인증값으로 활용 -> 우리는 토큰 인증 방식을 사용할 것이라서 비활성화 함.
                .httpBasic(AbstractHttpConfigurer::disable)
                // 세션방식을 비활성화 -> 로그인 방식은 크게 두 가지로 나뉠 수 있는데 세션 방식과 토큰 방식임.
                // 세션 방식은 로그인 인증 값을 서버에서 메모리 값으로 가지고 있는 것.
                // 토큰 방식은 서버에서 별도로 저장을 하지 않음.
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 특정 url 패턴에 대해서는 인증 처리 제외
                // 인증 처리란 Authentication 객체를 생성하는 것을 말한다.
                .authorizeHttpRequests(a -> a.requestMatchers("/member/create", "/member/doLogin").permitAll().anyRequest().authenticated())
                // UsernamePasswordAuthenticationFilter 이전에 jwtTokenFilter를 적용하겠다는 의미다.
                // UsernamePasswordAuthenticationFilter 이 클래스에서는 폼로그인 인증을 처리한다.
                // 뭔 말이냐면 폼로그인이라고 하는 것은 mvc 패턴에서 사용되는데 기본적으로 스프링에서 자체적으로 로그인 화면을 제공해줌.
                // 그리고 스프링에서는 기본적으로 폼 로그인 처리를 하려고 한다.
                // 근데 우리는 그 로그인을 사용하지 않고 우리가 만든 필터에서 토큰을 검증하고 authentication 객체를 생성했음.
                // 그래서 폼로그인으로 처리하기 전에 해당 필터로 authentication 객체를 만들겠다고 처리하는 거임.
                // 우리가 만든 필터를 가지고 직접 인증처리 하겠다고 보면 된다.
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 특정 도메인 허용
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 HTTP 메서드 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더값을 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용 -> Authrization 헤더를 사용할 수 있도록 설정하는 것

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 url 패턴에 대해서 cors 허용 설정
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}

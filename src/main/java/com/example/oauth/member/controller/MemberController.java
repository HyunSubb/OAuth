package com.example.oauth.member.controller;

import com.example.oauth.common.auth.JwtTokenProvider;
import com.example.oauth.member.domain.Member;
import com.example.oauth.member.domain.SocialType;
import com.example.oauth.member.dto.*;
import com.example.oauth.member.service.GoogleService;
import com.example.oauth.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;

    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, GoogleService googleService) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleService = googleService;
    }

    // 회원가입
    //<?>이면 Object(이렇게 되면 특정 클래스 말고 어떠한 클래스 든지 반환 가능함)로 반환하겠다는 거임.
    @PostMapping("/create")
    public ResponseEntity<?> memberCreate(@RequestBody MemberCreateDto memberCreateDto) {
        Member member = memberService.create(memberCreateDto);
        return new ResponseEntity<>(member.getId(), HttpStatus.CREATED);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto memberLoginDto) {
        // email, password 일치하는지 검증
        Member member = memberService.login(memberLoginDto);
        
        // 일치할 경우 jwt access token 생성
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }

    @PostMapping("/google/doLogin")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        // 1. accessToken 구글로부터 발급
        // getAccessToken을 할 때 사실 구글에서 토큰의 값 뿐만 아니라 여러가지 정보들을 준다.
        // 토큰의 만료 시간등이라던가 scope, 토큰의 타입 등... 추후에 활용을 할 수 있도록 String 말고 DTO로 만들어두기
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());

        // 2. 사용자 프로필 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());

        // 3. 회원가입이 되어있지 않다면 회원 가입
        // 소셜 아이디 값은 구글에서 주는 open id 이다. 로그인을 할 때마다 매번 동일한 값을 발급해줌.
        // 그래서 우리 서비스에서 회원가입이 되어 있는지 확인을 하려면 googleProfileDto에서 sub(구글에서 id값을 이런 이름으로 준다.)
        // 그 값을 꺼내서 db에 socialId가 저장되어 있는지 확인을 해보면 된다.
        Member originalMember = memberService.getMemberBySocialId(googleProfileDto.getSub());

        if(originalMember == null) {
            originalMember = memberService.createOauth(googleProfileDto.getSub(), googleProfileDto.getEmail(), SocialType.GOOGLE);
        }

        // 4. 회원가입이 되어있다면 우리서버의 JWT토큰 발급해주기
        String jwtToken = jwtTokenProvider.createToken(originalMember.getEmail(), originalMember.getRole().toString());

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", originalMember.getId());
        loginInfo.put("token", jwtToken);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }
}

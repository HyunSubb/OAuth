package com.example.oauth.member.service;

import com.example.oauth.member.dto.AccessTokenDto;
import com.example.oauth.member.dto.GoogleProfileDto;
import org.springframework.stereotype.Service;

@Service
public class GoogleService {
    public AccessTokenDto getAccessToken(String code) {

        return new AccessTokenDto();
    }

    public GoogleProfileDto getGoogleProfile(String token) {

        return new GoogleProfileDto();
    }
}

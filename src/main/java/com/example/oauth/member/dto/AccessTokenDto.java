package com.example.oauth.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 없는 필드는 자동으로 무시하겠다라는 어노테이션이다.
public class AccessTokenDto {
    private String access_token;
    private String expires_in;
    private String scope;
    private String id_token;
}

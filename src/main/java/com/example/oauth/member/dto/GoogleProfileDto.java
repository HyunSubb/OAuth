package com.example.oauth.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 지금 이 dto에 없는 필드값이 들어올 경우 그 필드는 자동으로 무시하겠다라는 어노테이션이다.
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleProfileDto {
    private String sub;
    private String email;
    private String picture;
}

package com.project_cuoimon.model.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TokenRefreshResponse {
    private String accessToken;
    private String refreshToken;
}

package com.example.myservice.modules.users.resources;

import lombok.*;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RefreshTokenResource {
    private String token;
    private String refreshToken;
}

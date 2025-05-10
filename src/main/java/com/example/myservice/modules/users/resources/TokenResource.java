package com.example.myservice.modules.users.resources;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class TokenResource {
    private final String token;
}

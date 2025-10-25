package com.brocode.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final TokenService tokenService;

    @PostMapping("/token")
    public TokenResponseDto token(Authentication authentication) {
        LOGGER.debug("Token requested for user: {}", authentication.getName());
        String token = tokenService.generateToken(authentication);
        LOGGER.debug("Token granted {}", token);
        return new TokenResponseDto(token);
    }
}

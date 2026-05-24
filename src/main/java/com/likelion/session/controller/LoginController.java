package com.likelion.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "인증 관련 API")
public class LoginController {

    @Operation(summary = "로그인", description = "username과 password를 입력하여 JWT 토큰을 발급받습니다.")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void login(
            @Parameter(description = "아이디") @RequestParam("username") String username,
            @Parameter(description = "비밀번호") @RequestParam("password") String password
    ) {
        // Spring Security LoginFilter가 처리
    }
}
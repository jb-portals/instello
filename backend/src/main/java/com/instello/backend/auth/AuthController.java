package com.instello.backend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String,Object>> getCsrfToken(CsrfToken csrfToken){
        return ResponseEntity.ok(Map.of(
                "data", Map.of(
                        "token", csrfToken.getToken(),
                        "headerName", csrfToken.getHeaderName(),
                        "parameterName", csrfToken.getParameterName()
                )
        ));
    }
}

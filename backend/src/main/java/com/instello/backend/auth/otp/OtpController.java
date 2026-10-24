package com.instello.backend.auth.otp;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController 
@RequestMapping ("/auth/otp")
public class OtpController {
    
    @PostMapping
    public String sendOtp() {
        return "123456";
    }
    
}

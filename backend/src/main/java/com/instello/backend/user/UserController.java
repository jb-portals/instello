package com.instello.backend.user;

import com.instello.backend.common.response.ApiResponse;
import com.instello.backend.user.dto.CurrentUserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name="users", description = "Users operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> me(@AuthenticationPrincipal String email){
        return ResponseEntity.ok(ApiResponse.ok("Current user", userService.getCurrentUser(email)));
    }
}

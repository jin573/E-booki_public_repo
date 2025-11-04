package com.be.ebooki.controller;

import com.be.ebooki.domain.User;
import com.be.ebooki.dto.UserRequest;
import com.be.ebooki.dto.UserResponse;
import com.be.ebooki.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //sign up
    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@Valid @RequestBody UserRequest.UserSignupDTO userSignupDTO) {
        UserResponse.UserInfoDTO registeredUser = userService.signupUser(userSignupDTO);

        UserResponse.UserResponseDTO<UserResponse.UserInfoDTO> responseDTO = UserResponse.UserResponseDTO.<UserResponse.UserInfoDTO>builder()
                .statusCode(200)
                .message("회원가입 성공")
                .data(registeredUser)
                .build();

        return ResponseEntity.ok(responseDTO);
    }



}

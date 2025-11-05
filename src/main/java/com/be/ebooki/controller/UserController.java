package com.be.ebooki.controller;

import com.be.ebooki.domain.User;
import com.be.ebooki.dto.UserRequest;
import com.be.ebooki.dto.UserResponse;
import com.be.ebooki.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

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

    //log in
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserRequest.UserLoginDTO userLoginDTO){
        //로그인 정보, 토큰 정보, 응답 코드 가져오기
        UserResponse.UserLoginDTO loginUser = userService.loginUser(userLoginDTO);

        UserResponse.UserResponseDTO<UserResponse.UserLoginDTO> responseDTO = UserResponse.UserResponseDTO.<UserResponse.UserLoginDTO>builder()
                .statusCode(200)
                .message("로그인 성공")
                .data(loginUser)
                .build();

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestBody UserRequest.TokenReissueDTO tokenReissueDTO) {
        UserResponse.TokenReissueDTO newToken = userService.reissueToken(tokenReissueDTO);

        UserResponse.UserResponseDTO<UserResponse.TokenReissueDTO> responseDTO =
                UserResponse.UserResponseDTO.<UserResponse.TokenReissueDTO>builder()
                        .statusCode(200)
                        .message("Access Token 재발급 성공")
                        .data(newToken)
                        .build();

        return ResponseEntity.ok(responseDTO);
    }


    //get all user
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse.UserInfoDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //test
    @GetMapping("/test")
    public ResponseEntity<String> testUser() {
        return ResponseEntity.ok("접근 성공");
    }
}

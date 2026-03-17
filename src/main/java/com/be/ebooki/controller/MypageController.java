package com.be.ebooki.controller;

import com.be.ebooki.dto.MypageResponse;
import com.be.ebooki.service.MypageService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<MypageResponse.MyPageDto> getMyPage() {
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(mypageService.getMyPage(userId));
    }
}
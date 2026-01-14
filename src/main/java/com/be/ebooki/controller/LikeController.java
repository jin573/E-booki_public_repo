package com.be.ebooki.controller;

import com.be.ebooki.domain.Book;
import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.service.LikeService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final UserService userService;

    //북마크 토글 (등록/해제)
    @PostMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Integer bookId) {
        Integer userId = userService.getCurrentUserId();

        String message = likeService.toggleLike(userId, bookId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);

        return ResponseEntity.ok(response);
    }

}

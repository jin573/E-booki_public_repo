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
    public ResponseEntity<BookResponse.ToggleDTO> toggleLike(
            @PathVariable Integer bookId) {
        Integer userId = userService.getCurrentUserId();

        return ResponseEntity.ok(
                likeService.toggleLike(userId, bookId)
        );
    }

}

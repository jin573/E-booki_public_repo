package com.be.ebooki.controller;
import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.service.LikeService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
        //사용자의 기존 토글여부 확인으로 userId 가져가기
        Integer userId = userService.getCurrentUserId();

        return ResponseEntity.ok(
                likeService.toggleLike(userId, bookId)
        );
    }

}

package com.be.ebooki.controller;

import com.be.ebooki.domain.EmojiType;
import com.be.ebooki.dto.ReadingRequest;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.service.ReadingService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;
    private final UserService userService;

    @GetMapping("/{bookId}/highlights")
    public ResponseEntity<ReadingResponse.HighlightListDTO> getHighlights(
            @PathVariable Integer bookId
    ) {
        return ResponseEntity.ok(readingService.getHighlights(bookId));
    }

    //특정 하이라이트 클릭 시 댓글 + 이모티콘 개수 조회
    @GetMapping("/highlights/{highlightId}/comments")
    public ResponseEntity<List<ReadingResponse.CommentDTO>> getComments(
            @PathVariable Integer highlightId) {
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(
                readingService.getComments(highlightId, userId)
        );
    }


    /** 하이라이트 생성 */
    @PostMapping("/highlights")
    public ResponseEntity<ReadingResponse.HighlightDTO> createHighlight(
            @RequestBody ReadingRequest.CreateHighlightDTO req
    ) {
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(
                readingService.createHighlight(req, userId)
        );
    }

    /** 댓글 생성 */
    @PostMapping("/comments")
    public ResponseEntity<ReadingResponse.CommentDTO> createComment(
            @RequestBody ReadingRequest.CreateCommentDTO req
    ) {
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(
                readingService.createComment(req, userId)
        );
    }

    @PostMapping("/comments/{commentId}/emoticons")
    public ResponseEntity<ReadingResponse.EmoticonCountDTO> toggle(
            @PathVariable Integer commentId,
            @RequestParam String type) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = (Integer) authentication.getPrincipal();

        EmojiType emoji = EmojiType.valueOf(type.toUpperCase());

        return ResponseEntity.ok(
                readingService.toggleEmoticon(commentId, userId, emoji)
        );
    }
}


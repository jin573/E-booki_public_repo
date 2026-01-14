package com.be.ebooki.controller;

import com.be.ebooki.dto.ReadingRequest;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.enums.EmojiType;
import com.be.ebooki.service.ReadingService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;
    private final UserService userService;

    //진입 시 마지막 위치 조회
    @GetMapping("/entry")
    public ResponseEntity<ReadingResponse.ReadingEntryDTO> getReadingEntry(
            @RequestParam Integer teamId,
            @RequestParam Integer bookId
    ) {
        Integer userId = userService.getCurrentUserId();

        ReadingResponse.ReadingEntryDTO response =
                readingService.getReadingEntry(userId, teamId, bookId);

        return ResponseEntity.ok(response);
    }


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
    @PostMapping("/{teamId}/highlights")
    public ResponseEntity<ReadingResponse.HighlightDTO> createHighlight(
            @PathVariable Integer teamId,
            @RequestBody ReadingRequest.CreateHighlightDTO req
    ) {
        Integer userId = userService.getCurrentUserId();

        return ResponseEntity.ok(
                readingService.createHighlight(req, userId, teamId)
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


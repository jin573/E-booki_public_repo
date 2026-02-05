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
            @RequestBody ReadingRequest.ReadingEntryDTO readingEntry
    ) {
        Integer userId = userService.getCurrentUserId();

        ReadingResponse.ReadingEntryDTO response =
                readingService.getReadingEntry(userId,readingEntry);

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
    /** 하이라이트 삭제 */
    @DeleteMapping("/teams/{teamId}/highlights/{highlightId}")
    public ResponseEntity<Void> deleteHighlight(
            @PathVariable Integer teamId,
            @PathVariable Integer highlightId
    ) {
        Integer userId = userService.getCurrentUserId();

        readingService.deleteHighlight(highlightId, userId, teamId);

        return ResponseEntity.noContent().build();
    }
    /** 댓글 생성 */
    @PostMapping("/{teamId}/comments")
    public ResponseEntity<ReadingResponse.CommentDTO> createComment(
            @PathVariable Integer teamId,
            @RequestBody ReadingRequest.CreateCommentDTO req
    ) {
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(
                readingService.createComment(req, userId, teamId)
        );
    }
    /** 댓글 삭제 */
    @DeleteMapping("/teams/{teamId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer teamId,
            @PathVariable Integer commentId
    ) {
        Integer userId = userService.getCurrentUserId();

        readingService.deleteComment(commentId, userId, teamId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/emoticons")
    public ResponseEntity<ReadingResponse.EmoticonCountDTO> toggle(
            @RequestBody ReadingRequest.CreateEmoticonsDTO req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = (Integer) authentication.getPrincipal();

        EmojiType emoji = EmojiType.valueOf(req.getType().toUpperCase());

        return ResponseEntity.ok(
                readingService.toggleEmoticon(req.getCommentId(), req.getTeamId(), userId, emoji)
        );
    }

}


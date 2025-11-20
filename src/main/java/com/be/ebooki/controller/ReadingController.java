package com.be.ebooki.controller;

import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @GetMapping("/{bookId}/highlights")
    public ResponseEntity<ReadingResponse.HighlightListDTO> getHighlights(
            @PathVariable Integer bookId
    ) {
        return ResponseEntity.ok(readingService.getHighlights(bookId));
    }

    // ② 특정 하이라이트 → 댓글 + 이모티콘 개수 조회
    @GetMapping("/highlights/{highlightId}/comments")
    public ResponseEntity<List<ReadingResponse.CommentDTO>> getComments(
            @PathVariable Integer highlightId
    ) {
        return ResponseEntity.ok(readingService.getComments(highlightId));
    }
}


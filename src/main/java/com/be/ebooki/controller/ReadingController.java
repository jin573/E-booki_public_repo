package com.be.ebooki.controller;

import com.be.ebooki.dto.HighlightRequest;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //하이라이트 생성
    @PostMapping("/highlight")
    public ResponseEntity<?> createHighlight(@RequestBody HighlightRequest request){
        readingService.createHighlight(request);
        return ResponseEntity.ok().build();
    }

}


package com.be.ebooki.controller;

import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.service.BookService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<BookResponse.BookListDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse.BookDetailDTO> getBookDetail(@PathVariable Integer bookId) {
        //좋아요 여부 판단을 위해 userId 가져감
        Integer userId = userService.getCurrentUserId();

        return ResponseEntity.ok(bookService.getBookDetail(bookId, userId));
    }

    @GetMapping("/{bookId}/timeline")
    public ResponseEntity<List<ReadingResponse.ReadingTimelineItemDTO>> getReadingTimeline(
            @PathVariable Integer bookId,
            @RequestParam(defaultValue = "ALL") String type
    ) {
        //현재 사용자가 작성한 내용만 보여주가 위해 userId가져감
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(
                bookService.getBookReadingDetail(bookId, userId,type)
        );
    }

}

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

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse.BookDetailDTO> getBookDetail(@PathVariable Integer id) {
        //userId가져오기
        Integer userId = userService.getCurrentUserId();

        return ResponseEntity.ok(bookService.getBookDetail(id, userId));
    }

    @GetMapping("/books/{bookId}/reading-timeline")
    public ResponseEntity<List<ReadingResponse.ReadingTimelineItemDTO>> getReadingTimeline(
            @PathVariable Integer bookId,
            @RequestParam(defaultValue = "ALL") String type
    ) {
        Integer userId = userService.getCurrentUserId();
        return ResponseEntity.ok(
                bookService.getBookReadingDetail(bookId, userId,type)
        );
    }

}

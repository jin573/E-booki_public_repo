package com.be.ebooki.controller;

import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookResponse.BookListDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse.BookDetailDTO> getBookDetail(@PathVariable Integer id) {
        //userId가져오기
        return ResponseEntity.ok(bookService.getBookDetail(id));
    }
}

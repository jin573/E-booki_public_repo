package com.be.ebooki.controller;

import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<BookResponse.SearchResponse> search(@RequestParam String q) {
        return ResponseEntity.ok(searchService.searchBook(q));
    }
}

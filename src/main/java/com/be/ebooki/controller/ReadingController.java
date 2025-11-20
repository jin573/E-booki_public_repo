package com.be.ebooki.controller;

import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @GetMapping("/{bookId}/enter")
    public ResponseEntity<ReadingResponse.EnterDTO> enter(
            @PathVariable Integer bookId
    ) {
        ReadingResponse.EnterDTO response = readingService.getInitialData(bookId);
        return ResponseEntity.ok(response);
    }
}


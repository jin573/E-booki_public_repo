package com.be.ebooki.service;

import com.be.ebooki.domain.Book;
import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final BookRepository bookRepository;

    public BookResponse.SearchResponse searchBook(String q) {

        String query = (q == null) ? "" : q.trim();

        Pageable pageable = PageRequest.of(0, 10);  // 최대 10개 반환

        Page<Book> resultPage = bookRepository.searchBooks(query, pageable);
        List<Book> results = resultPage.getContent();

        // Book → BookListDTO 변환
        List<BookResponse.BookListDTO> items = results.stream()
                .map(b -> BookResponse.BookListDTO.builder()
                        .id(b.getId())
                        .title(b.getTitle())
                        .bookImage(b.getBookImage())
                        .rating(b.getRating())
                        .build())
                .toList();

        return BookResponse.SearchResponse.builder()
                .query(query)
                .bookList(items)
                .totalBooks((int) resultPage.getTotalElements())
                .build();
    }
}


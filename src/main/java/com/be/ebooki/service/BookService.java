package com.be.ebooki.service;

import com.be.ebooki.domain.Book;
import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.repository.BookRepository;
import com.be.ebooki.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LikeRepository likeRepository;

    public List<BookResponse.BookListDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(book -> BookResponse.BookListDTO.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .bookImage(book.getBookImage())
                        .rating(book.getRating())
                        .build())
                .collect(Collectors.toList());
    }

    public BookResponse.BookDetailDTO getBookDetail(Integer bookId, Integer userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다." + bookId));
        boolean liked = likeRepository.existsByUserIdAndBookId(userId, bookId);

        return BookResponse.BookDetailDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .price(book.getPrice())
                .bookImage(book.getBookImage())
                .rating(book.getRating())
                .liked(liked)
                .build();
    }

    public BookResponse.BookPreviewDTO getBookPreview(Integer bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다." + bookId));

        return BookResponse.BookPreviewDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .bookImage(book.getBookImage())
                .build();
    }
}

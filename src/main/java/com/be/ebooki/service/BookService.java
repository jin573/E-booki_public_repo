package com.be.ebooki.service;

import com.be.ebooki.domain.Book;
import com.be.ebooki.enums.TimelineType;
import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.enums.TimelineType;
import com.be.ebooki.repository.BookRepository;
import com.be.ebooki.repository.CommentRepository;
import com.be.ebooki.repository.HighlightRepository;
import com.be.ebooki.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final LikeRepository likeRepository;

    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;

    public List<BookResponse.BookListDTO> getAllBooks() {
        //findAll로 DB도서 전체 가져옴
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
        //조회하려는 책 찾기
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다." + bookId));
        //userId와 bookId가지고 좋아요 여부 찾기
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

    @Transactional(readOnly = true)
    public List<ReadingResponse.ReadingTimelineItemDTO> getBookReadingDetail(Integer bookId, Integer userId,
                                                                             String type) {
        //조회하려는 필터링 타입으로 맞추기
        TimelineType timelineType = TimelineType.valueOf(type);

        // 책의 존재 여부 체크
        bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));

        //사용자가 생성한 하이라이트와 댓글을 담을 List 생성
        List<ReadingResponse.ReadingTimelineItemDTO> highlights = List.of();
        List<ReadingResponse.ReadingTimelineItemDTO> comments = List.of();

        //조건을 가지고 하이라이트 찾기
        if (timelineType == TimelineType.ALL || timelineType == TimelineType.HIGHLIGHT) {
            highlights = highlightRepository.findAllByBookId(bookId)
                    .stream()
                    .map(h -> ReadingResponse.ReadingTimelineItemDTO.builder()
                            .type("HIGHLIGHT")
                            .text(h.getText())
                            .spineIndex(h.getSpineIndex())
                            .cfi(h.getCfi())
                            .createdAt(h.getCreatedAt())
                            .build()
                    )
                    .toList();
        }

        //조건을 가지고 코멘트 찾기
        if (timelineType == TimelineType.ALL || timelineType == TimelineType.COMMENT) {
            comments = commentRepository.findAllByBookId(bookId)
                    .stream()
                    .map(c -> ReadingResponse.ReadingTimelineItemDTO.builder()
                            .type("COMMENT")
                            .text(c.getText())
                            .createdAt(c.getCreatedAt())
                            .build()
                    )
                    .toList();
        }

        //최신순으로 정렬
        return Stream.concat(highlights.stream(), comments.stream())
                .sorted(
                        Comparator.comparing(
                                ReadingResponse.ReadingTimelineItemDTO::getCreatedAt
                        ).reversed()
                )
                .toList();
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

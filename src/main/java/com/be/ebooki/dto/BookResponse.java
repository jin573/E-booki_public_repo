package com.be.ebooki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class BookResponse {

    @Data
    @Builder
    @AllArgsConstructor
    public static class BookListDTO {
        private Integer id;
        private String title;
        private String bookImage;
        private Double rating;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class BookDetailDTO {
        private Integer id;
        private String title;
        private String author;
        private String publisher;
        private Integer price;
        private String bookImage;
        private Double rating;

        private boolean liked;
    }

    @Data
    @Builder
    public static class SearchResponse {
        private String query;
        private List<BookListDTO> bookList;
        private Integer totalBooks;
    }
}

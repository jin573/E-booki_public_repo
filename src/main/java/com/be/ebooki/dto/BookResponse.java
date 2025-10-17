package com.be.ebooki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BookResponse {

    @Data
    @Builder
    @AllArgsConstructor
    public static class BookListDTO {
        private Integer id;
        private String title;
        private String bookImage;
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
    }
}

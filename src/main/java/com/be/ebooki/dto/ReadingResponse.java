package com.be.ebooki.dto;

import lombok.*;

import java.util.List;

public class ReadingResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighlightDTO {
        private Integer id;
        private Integer userId;
        private Integer teamId;
        private Integer bookId;
        private Integer spineIndex;
        private String cfi;
        private String text;
        private String color;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighlightListDTO {
        private Integer bookId;
        private List<HighlightDTO> highlights;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDTO {
        private Integer id;
        private Integer userId;
        private Integer highlightId;
        private String text;
        private Long createdAt;
        private EmoticonCountDTO emoticons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmoticonCountDTO {
        private int likeCount;
        private int cryCount;
    }
}

package com.be.ebooki.dto;

import lombok.*;

import java.util.List;

public class ReadingResponse {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnterDTO {
        private Integer bookId;
        private List<HighlightDTO> highlights;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HighlightDTO {
        private Integer id;
        private Integer userId;
        private Integer teamId;
        private Integer spineIndex;
        private String cfi;
        private String text;
        private String color;
        private Long createdAt;

        private List<CommentDTO> comments;
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

        private List<EmoticonDTO> emoticons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmoticonDTO {
        private Integer id;
        private Integer userId;
        private Integer commentId;
        private String emoji;
        private Long createdAt;
    }
}

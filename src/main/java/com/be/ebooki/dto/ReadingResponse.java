package com.be.ebooki.dto;

import lombok.*;

import java.time.LocalDateTime;
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
        private LocalDateTime createdAt;
        private EmoticonCountDTO emoticons;
        private UserEmoticonDTO myEmoticon;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserEmoticonDTO {
        private boolean smiled;
        private boolean liked;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmoticonCountDTO {
        private int smileCount;
        private int likeCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public class ReadingTimelineItemDTO {

        private String type; // "HIGHLIGHT" | "COMMENT"

        private String text;

        private Integer spineIndex; // 하이라이트만
        private String cfi;         // 하이라이트만

        private LocalDateTime createdAt;
    }

}

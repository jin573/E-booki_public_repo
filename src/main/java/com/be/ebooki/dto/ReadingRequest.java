package com.be.ebooki.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

public class ReadingRequest {

    @Data
    public static class CreateHighlightDTO {
        private Integer bookId;
        private Integer spineIndex;
        private String cfi;
        private String text;
    }

    @Data
    public static class CreateCommentDTO {
        private Integer highlightId;
        private String text;
    }

    @Data
    public static class CreateEmoticonsDTO {
        private Integer commentId;
        private String type;
    }

    @Data
    public static class UpdateCommentDTO {
        private String text;
    }

    @Data
    public static class ProgressRequest {

        private Integer spineIndex;
        private String cfi;
        private Double percent;
    }
}

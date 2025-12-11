package com.be.ebooki.dto;

import lombok.Data;

public class ReadingRequest {

    @Data
    public static class CreateHighlightDTO {
        private Integer bookId;
        private Integer spineIndex;
        private String cfi;
        private String text;
        private String color;
    }

    @Data
    public static class CreateCommentDTO {
        private Integer highlightId;
        private String text;
    }
}

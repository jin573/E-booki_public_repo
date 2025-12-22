package com.be.ebooki.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HighlightRequest {

    private Integer bookId;
    private Integer teamId;
    private Integer spineIndex;
    private String cfi;
    private String text;
    private String color;

}

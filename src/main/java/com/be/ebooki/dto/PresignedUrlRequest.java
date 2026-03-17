package com.be.ebooki.dto;

import lombok.Getter;

@Getter
public class PresignedUrlRequest {
    private String fileName;
    private String contentType;
}

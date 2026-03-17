package com.be.ebooki.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PresignedUrlResponse {
    private String presignedUrl;
    private String fileKey;
}

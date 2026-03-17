package com.be.ebooki.controller;

import com.be.ebooki.dto.PresignedUrlRequest;
import com.be.ebooki.dto.PresignedUrlResponse;
import com.be.ebooki.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/presigned")
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;

    @PostMapping("/profile")
    public PresignedUrlResponse getPresignedUrl(
            @RequestBody PresignedUrlRequest presignedUrlRequest
            ) {
        return s3Service.generateProfilePresignedUrl(presignedUrlRequest.getFileName(), presignedUrlRequest.getContentType());
    }
}

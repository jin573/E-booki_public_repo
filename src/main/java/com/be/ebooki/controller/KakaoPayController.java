package com.be.ebooki.controller;

import com.be.ebooki.dto.KakaoApproveResponse;
import com.be.ebooki.dto.KakaoReadyResponse;
import com.be.ebooki.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    @PostMapping("/ready")
    public KakaoReadyResponse readyToKakaoPay(@RequestParam Long planId) {
        return kakaoPayService.kakaoPayReady(planId);
    }

    @PostMapping("/success")
    public ResponseEntity<KakaoApproveResponse> afterPayRequest(
            @RequestParam("pg_token") String pgToken) {

        KakaoApproveResponse kakaoApprove = kakaoPayService.approveResponse(pgToken);
        return new ResponseEntity<>(kakaoApprove, HttpStatus.OK);
    }

    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancel() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "CANCEL");
        response.put("message", "결제가 취소되었습니다.");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/fail")
    public ResponseEntity<Map<String, String>> fail() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "FAILED");
        response.put("message", "결제에 실패했습니다.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}

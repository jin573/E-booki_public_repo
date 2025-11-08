package com.be.ebooki.service;

import com.be.ebooki.dto.KakaoApproveResponse;
import com.be.ebooki.dto.KakaoReadyResponse;
import com.be.ebooki.pay.KakaoPayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoPayService {

    private final KakaoPayProperties payProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private KakaoReadyResponse kakaoReady;


    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "SECRET_KEY " + payProperties.getSecretKey();
        headers.set("Authorization", auth);
        headers.set("Content-Type", "application/json");
        log.info("🔐 Authorization Header = {}", auth);  // 👈 반드시 INFO 이상 레벨
        return headers;
    }

    public KakaoReadyResponse kakaoPayReady() {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("cid", payProperties.getCid());
        parameters.put("partner_order_id", "ORDER_ID");  // 실제 주문번호로 교체
        parameters.put("partner_user_id", "USER_ID");    // 실제 사용자 ID로 교체
        parameters.put("item_name", "ITEM_NAME");        // 실제 상품명으로 교체
        parameters.put("quantity", "1");                 // 수량
        parameters.put("total_amount", "2200");          // 총 금액
        parameters.put("vat_amount", "200");             // 부가세
        parameters.put("tax_free_amount", "0");          // 비과세 금액
        parameters.put("approval_url", "http://localhost:8080/payment/success");
        parameters.put("cancel_url", "http://localhost:8080/payment/cancel");
        parameters.put("fail_url", "http://localhost:8080/payment/fail");


        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(parameters, this.getHeaders());

        kakaoReady = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                requestEntity,
                KakaoReadyResponse.class
        );

        return kakaoReady;
    }
    public KakaoApproveResponse approveResponse(String pgToken) {

        // 카카오 요청 파라미터
        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payProperties.getCid());
        parameters.put("tid", kakaoReady.getTid());
        parameters.put("partner_order_id", "ORDER_ID");
        parameters.put("partner_user_id", "USER_ID");
        parameters.put("pg_token", pgToken);

        // 파라미터 + 헤더
        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(parameters, this.getHeaders());

        System.out.println();
        System.out.println("===== [KAKAO APPROVE REQUEST] =====");
        System.out.println(requestEntity);
        System.out.println("===================================");

        RestTemplate restTemplate = new RestTemplate();
        KakaoApproveResponse approveResponse = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/approve",
                requestEntity,
                KakaoApproveResponse.class
        );

        System.out.println();
        System.out.println("===== [KAKAO APPROVE RESPONSE] =====");
        System.out.println(approveResponse);
        System.out.println("====================================");
        System.out.println();

        return approveResponse;
    }


}

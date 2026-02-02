package com.be.ebooki.service;

import com.be.ebooki.domain.*;
import com.be.ebooki.enums.PaymentStatus;
import com.be.ebooki.enums.UserPlanStatus;
import com.be.ebooki.dto.KakaoApproveResponse;
import com.be.ebooki.dto.KakaoReadyResponse;
import com.be.ebooki.pay.KakaoPayProperties;
import com.be.ebooki.repository.PaymentRepository;
import com.be.ebooki.repository.PlanRepository;
import com.be.ebooki.repository.UserPlanRepository;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoPayService {

    private final KakaoPayProperties payProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final UserService userService;
    private final PaymentRepository paymentRepository;
    private final UserPlanRepository userPlanRepository;

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "SECRET_KEY " + payProperties.getSecretKey();
        headers.set("Authorization", auth);
        headers.set("Content-Type", "application/json");
        log.info("Authorization Header = {}", auth);
        return headers;
    }

    @Transactional
    public KakaoReadyResponse kakaoPayReady(Long planId) {

        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("사용자 정보가 없습니다."));

        if (userPlanRepository.existsByUserAndStatus(user, UserPlanStatus.ACTIVE)) {
            throw new IllegalStateException("이미 사용 중인 요금제가 있습니다.");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제입니다."));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", payProperties.getCid());
        parameters.put("partner_order_id", "ORDER_ID");
        parameters.put("partner_user_id", user.getId().toString());
        parameters.put("item_name", plan.getName());
        parameters.put("quantity", "1");
        parameters.put("total_amount", plan.getPrice());
        parameters.put("vat_amount", "0");
        parameters.put("tax_free_amount", "0");
        parameters.put("approval_url", "http://localhost:8080/payment/success");
        parameters.put("cancel_url", "http://localhost:8080/payment/cancel");
        parameters.put("fail_url", "http://localhost:8080/payment/fail");

        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(parameters, getHeaders());

        KakaoReadyResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                requestEntity,
                KakaoReadyResponse.class
        );

        if (response == null || response.getTid() == null) {
            throw new IllegalStateException("카카오페이 결제 준비에 실패했습니다.");
        }

        // READY 상태 Payment 먼저 생성 -> 추후 결제완료시 업데이트 하는 방향으로
        Payment payment = Payment.builder()
                .user(user)
                .plan(plan)
                .status(PaymentStatus.READY)
                .pgTid(response.getTid())
                .build();

        paymentRepository.save(payment);

        return response;
    }

    @Transactional
    public KakaoApproveResponse approveResponse(String pgToken, String tid) {

        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", payProperties.getCid());
        parameters.put("tid", tid);
        parameters.put("partner_order_id", "ORDER_ID");
        parameters.put("partner_user_id", "USER_ID");
        parameters.put("pg_token", pgToken);

        HttpEntity<Map<String, String>> requestEntity =
                new HttpEntity<>(parameters, getHeaders());

        KakaoApproveResponse approveResponse = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/approve",
                requestEntity,
                KakaoApproveResponse.class
        );

        // READY 상태 Payment 조회
        Payment payment = paymentRepository.findByPgTid(tid)
                .orElseThrow(() -> new IllegalStateException("결제 정보가 존재하지 않습니다."));

        // 승인 처리 (update)
        payment.approve(
                approveResponse.getAmount().getTotal(),
                approveResponse.getTid(),
                LocalDateTime.now()
        );

        // UserPlan 생성
        UserPlan userPlan = UserPlan.builder()
                .user(payment.getUser())
                .plan(payment.getPlan())
                .totalBookCount(payment.getPlan().getTotalBookCount())
                .build();

        userPlanRepository.save(userPlan);

        return approveResponse;
    }



}

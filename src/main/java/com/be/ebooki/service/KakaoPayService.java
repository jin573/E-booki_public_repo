package com.be.ebooki.service;

import com.be.ebooki.domain.*;
import com.be.ebooki.dto.KakaoApproveResponse;
import com.be.ebooki.dto.KakaoReadyResponse;

import com.be.ebooki.enums.PaymentStatus;
import com.be.ebooki.enums.UserPlanStatus;
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
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class KakaoPayService {

    private final KakaoPayProperties payProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private KakaoReadyResponse kakaoReady;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final UserService userService;
    private final PaymentRepository paymentRepository;
    private final UserPlanRepository userPlanRepository;
    private Long currentPlanId;




    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "SECRET_KEY " + payProperties.getSecretKey();
        headers.set("Authorization", auth);
        headers.set("Content-Type", "application/json");
        log.info("🔐 Authorization Header = {}", auth);  // 👈 반드시 INFO 이상 레벨
        return headers;
    }

    @Transactional
    public KakaoReadyResponse kakaoPayReady(Long planId) {
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new IllegalStateException("사용자 정보가 없습니다."));

        boolean exists =
                userPlanRepository.existsByUserAndStatus(user, UserPlanStatus.ACTIVE);

        if (exists) {
            throw new IllegalStateException("이미 사용 중인 요금제가 있습니다.");
        }

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제입니다."));
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("cid", payProperties.getCid());
        parameters.put("partner_order_id", "ORDER_ID");  // 실제 주문번호로 교체
        parameters.put("partner_user_id", "USER_ID");    // 실제 사용자 ID로 교체
        parameters.put("item_name", plan.getName());        // 실제 상품명으로 교체
        parameters.put("quantity", "1");                 // 수량
        parameters.put("total_amount", plan.getPrice());          // 총 금액
        parameters.put("vat_amount", "0");             // 부가세
        parameters.put("tax_free_amount", "0");          // 비과세 금액
        parameters.put("approval_url", "http://localhost:8080/payment/success");
        parameters.put("cancel_url", "http://localhost:8080/payment/cancel");
        parameters.put("fail_url", "http://localhost:8080/payment/fail");


        HttpEntity<Map<String, Object>> requestEntity =
                new HttpEntity<>(parameters, this.getHeaders());

        KakaoReadyResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                requestEntity,
                KakaoReadyResponse.class
        );
        this.kakaoReady = response;
        this.currentPlanId = planId;

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
       User user = userRepository.findById(userService.getCurrentUserId())
               .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Long planId = kakaoReady.getPlanId();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제입니다."));


        Payment payment = Payment.builder()
                .user(user)
                .plan(plan)
                .paidAmount(approveResponse.getAmount().getTotal())
                .pgTid(approveResponse.getTid())
                .paymentMethod("KAKAO_PAY")
                .status(PaymentStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .build();


        paymentRepository.save(payment);

        // 5. UserPlan 생성 + 저장
        UserPlan userPlan = UserPlan.builder()
                .user(user)
                .plan(plan)
                .totalBookCount(plan.getTotalBookCount())
                .build();

        userPlanRepository.save(userPlan);

        return approveResponse;
    }


}

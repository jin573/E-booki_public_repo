package com.be.ebooki.domain;

public enum PaymentStatus {
    READY,       // 결제 준비
    APPROVED,    // 결제 승인 완료
    CANCELLED    // 결제 취소 / 환불
}

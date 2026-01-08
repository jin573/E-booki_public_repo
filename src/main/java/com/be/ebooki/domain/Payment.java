package com.be.ebooki.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payment")
public class Payment {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Plan plan;

    private int paidAmount;        // 실제 결제 금액
    private String pgTid;          // 카카오페이 TID
    private String paymentMethod;  // KAKAO_PAY

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;  // READY, APPROVED, CANCELLED

    private LocalDateTime approvedAt;
}

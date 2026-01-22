package com.be.ebooki.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.be.ebooki.domain.*;

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
    private com.be.ebooki.domain.PaymentStatus status;  // READY, APPROVED, CANCELLED

    private LocalDateTime approvedAt;

    public void approve(int paidAmount, String tid, LocalDateTime approvedAt) {
        this.paidAmount = paidAmount;
        this.pgTid = tid;
        this.status = com.be.ebooki.domain.PaymentStatus.APPROVED;
        this.approvedAt = approvedAt;
    }

    @Builder
    private Payment(
            User user,
            Plan plan,
            int paidAmount,
            String pgTid,
            String paymentMethod,
            com.be.ebooki.domain.PaymentStatus status,
            LocalDateTime approvedAt
    ) {
        this.user = user;
        this.plan = plan;
        this.paidAmount = paidAmount;
        this.pgTid = pgTid;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.approvedAt = approvedAt;
    }
}

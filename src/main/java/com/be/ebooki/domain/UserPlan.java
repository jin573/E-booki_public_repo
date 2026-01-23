package com.be.ebooki.domain;
import com.be.ebooki.enums.UserPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_plan")
public class UserPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Plan plan;

    @Column(nullable = false)
    private int totalBookCount;

    @Column(nullable = false)
    private int usedBookCount;

    @Enumerated(EnumType.STRING)
    private UserPlanStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;

    public void setUsedBookCount(int usedBookCount) {
        this.usedBookCount = usedBookCount;
    }

    public void expire() {
        this.status = UserPlanStatus.EXPIRED;
        this.expiredAt = LocalDateTime.now();
    }

    @Builder
    private UserPlan(
            User user,
            Plan plan,
            int totalBookCount
    ) {
        this.user = user;
        this.plan = plan;
        this.totalBookCount = totalBookCount;
        this.usedBookCount = 0;
        this.status = UserPlanStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }
}

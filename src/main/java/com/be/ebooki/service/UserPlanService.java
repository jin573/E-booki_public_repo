package com.be.ebooki.service;
import com.be.ebooki.domain.User;
import com.be.ebooki.domain.UserPlan;
import com.be.ebooki.enums.UserPlanStatus;
import com.be.ebooki.repository.UserPlanRepository;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPlanService {
    private final UserPlanRepository userPlanRepository;

    public void validateActivePlan(User user){
        boolean exists =
                userPlanRepository.existsByUserAndStatus(user, UserPlanStatus.ACTIVE);

        if (!exists) {
            throw new IllegalStateException("사용 가능한 요금제가 없습니다.");
        }
    }
    @Transactional
    public void consumeOneBook(User user) {

        UserPlan userPlan = userPlanRepository
                .findByUserAndStatus(user, UserPlanStatus.ACTIVE)
                .orElseThrow(() ->
                        new IllegalStateException("사용 가능한 요금제가 없습니다."));
        int next = userPlan.getUsedBookCount() + 1;
        userPlan.setUsedBookCount(next);

        if (next >= userPlan.getTotalBookCount()) {
            userPlan.expire();
        }

    }
}

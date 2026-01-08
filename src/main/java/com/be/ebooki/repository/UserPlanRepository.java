package com.be.ebooki.repository;

import com.be.ebooki.domain.User;
import com.be.ebooki.domain.UserPlan;
import com.be.ebooki.domain.UserPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPlanRepository extends JpaRepository<UserPlan, Long> {

    // 현재 사용 중 요금제가 있는지 확인
    boolean existsByUserAndStatus(User user, UserPlanStatus status);

    // 현재 사용 중 요금제 조회
    Optional<UserPlan> findByUserAndStatus(User user, UserPlanStatus status);
}

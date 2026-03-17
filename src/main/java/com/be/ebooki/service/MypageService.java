package com.be.ebooki.service;

import com.be.ebooki.domain.UserBookProgress;
import com.be.ebooki.domain.UserPlan;
import com.be.ebooki.dto.MypageResponse;
import com.be.ebooki.enums.UserPlanStatus;
import com.be.ebooki.repository.UserBookProgressRepository;
import com.be.ebooki.repository.UserPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageService {

    private final UserPlanRepository userPlanRepository;
    private final UserBookProgressRepository userBookProgressRepository;

    public MypageResponse.MyPageDto getMyPage(Integer userId) {

        // 1. 현재 ACTIVE 상태인 요금제 조회 (없으면 null)
        UserPlan activeUserPlan = userPlanRepository
                .findByUserIdAndStatus(userId, UserPlanStatus.ACTIVE)
                .orElse(null);

        // 2. 요금제 관련 정보 추출 (요금제 없으면 null로)
        String planName = null;
        Integer totalBookCount = null;
        Integer usedBookCount = null;

        if (activeUserPlan != null) {
            planName = activeUserPlan.getPlan().getName();
            totalBookCount = activeUserPlan.getPlan().getTotalBookCount();
            usedBookCount = activeUserPlan.getUsedBookCount();
        }

        // 3. 전체 읽은 책 목록 조회 (percent = 100 기준)
        List<UserBookProgress> completedBooks = userBookProgressRepository
                .findByUserIdAndPercent(userId, 100.0);

        // 4. 총 읽은 책 수
        int totalReadCount = completedBooks.size();

        // 5. 카테고리별 집계
        //    Book에 category가 null인 경우 "미분류"로 처리
        Map<String, Long> categoryCountMap = completedBooks.stream()
                .collect(Collectors.groupingBy(
                        progress -> {
                            String category = progress.getBook().getCategory();
                            return category != null ? category : "미분류";
                        },
                        Collectors.counting()
                ));

        List<MypageResponse.CategoryBookCountDto> categoryStats = categoryCountMap.entrySet().stream()
                .map(entry -> MypageResponse.CategoryBookCountDto.builder()
                        .category(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());

        return MypageResponse.MyPageDto.builder()
                .planName(planName)
                .totalBookCount(totalBookCount)
                .usedBookCount(usedBookCount)
                .totalReadCount(totalReadCount)
                .categoryStats(categoryStats)
                .build();
    }
}
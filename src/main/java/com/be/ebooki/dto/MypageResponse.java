package com.be.ebooki.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class MypageResponse {

    @Getter
    @Builder
    public static class MyPageDto {

        // 요금제 정보 (요금제 미가입 시 null)
        private String planName;          // Plan.name
        private Integer totalBookCount;   // Plan.totalBookCount (요금제가 가질 수 있는 총 권 수)
        private Integer usedBookCount;    // UserPlan.usedBookCount (현 요금제에서 사용한 권 수)

        // 독서 분석
        private int totalReadCount;                      // 전체 읽은 책 수 (percent = 100 기준)
        private List<CategoryBookCountDto> categoryStats; // 카테고리별 읽은 책 수
    }

    @Getter
    @Builder
    public static class CategoryBookCountDto {
        private String category;   // 카테고리명
        private int count;         // 해당 카테고리에서 읽은 책 수
    }
}
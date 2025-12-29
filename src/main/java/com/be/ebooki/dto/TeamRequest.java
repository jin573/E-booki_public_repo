package com.be.ebooki.dto;


import lombok.*;

@Getter
public class TeamRequest {
    @Getter
    @Builder
    public static class TeamInitDTO{
        private String teamName;
        private Integer bookId;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateTeamName {
        private String teamName;
    }

}

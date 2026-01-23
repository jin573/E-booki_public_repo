package com.be.ebooki.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.*;

@Getter
public class TeamRequest {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
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

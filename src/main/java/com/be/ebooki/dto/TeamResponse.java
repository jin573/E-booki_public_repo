package com.be.ebooki.dto;

import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class TeamResponse {

    @Getter
    @Builder
    public static class TeamResponseDTO<T>{
        private int statusCode;
        private String message;

        private T data;

    }

    @Getter
    @Builder
    public static class TeamInfoDTO{
        private TeamDTO teamData;
        private TeamUserDTO teamUserData;
        private String inviteUrl;
    }

    @Getter
    @Builder
    public static class TeamDTO{
        private Integer id;
        private String teamName;

        public static TeamDTO from(Team team) {
            return TeamDTO.builder()
                    .id(team.getId())
                    .teamName(team.getTeamName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TeamUserDTO{
        private Integer id;
        private Integer userId;
        private Integer teamId;
        public static TeamUserDTO from(TeamUser teamUser) {
            return TeamUserDTO.builder()
                    .id(teamUser.getId())
                    .userId(teamUser.getUser().getId())
                    .teamId(teamUser.getTeam().getId())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TeamListItemDTO {
        private Integer teamId;
        private String teamName;

        private String bookTitle;
        private String bookImage;

        private List<String> memberProfileImages;
    }

    @Getter
    @Builder
    public static class TeamListResponse {
        private List<TeamListItemDTO> teams;
    }







}

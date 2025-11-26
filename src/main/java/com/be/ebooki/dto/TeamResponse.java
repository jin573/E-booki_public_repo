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
    public static class TeamResponseDTO<T1, T2>{
        private int statusCode;
        private String message;

        private T1 teamData;
        private T2 bookData;

    }

    @Getter
    @Builder
    public static class TeamInfoDTO{
        private TeamDTO teamData;
        private List<TeamUserDTO> teamUserData;
        private String inviteUrl;
    }

    @Getter
    @Builder
    public static class TeamDTO{
        private Integer id;
        private String teamName;

        private Integer bookId;

        public static TeamDTO from(Team team) {
            return TeamDTO.builder()
                    .id(team.getId())
                    .teamName(team.getTeamName())
                    .bookId(team.getBookId())
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
}

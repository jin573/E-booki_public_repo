package com.be.ebooki.dto;

import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import com.be.ebooki.enums.UserColor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

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
                    .bookId(team.getBook().getId())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class TeamUserDTO{
        private Integer id;
        private Integer userId;
        private Integer teamId;
        private UserColor userColor;
        public static TeamUserDTO from(TeamUser teamUser) {
            return TeamUserDTO.builder()
                    .id(teamUser.getId())
                    .userId(teamUser.getUser().getId())
                    .teamId(teamUser.getTeam().getId())
                    .userColor(teamUser.getUserColor())
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

        private Integer progressPercentage;

        // progress가 100이고, 사용자가 별점을 안매긴 경우만 true
        private Boolean canRate;
        //팀평균 별점 -> 없을땐 null
        private Double averageRating;
    }

    @Getter
    @Builder
    public static class TeamListResponse {
        private List<TeamListItemDTO> teams;
    }


    @Getter
    @Builder
    public static class ReissueTeamUrlResponse{
        private Integer teamId;
        private String newUrl;
    }




}

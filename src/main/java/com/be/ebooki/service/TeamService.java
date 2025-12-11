package com.be.ebooki.service;

import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import com.be.ebooki.domain.User;
import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.repository.TeamRepository;
import com.be.ebooki.repository.TeamUserRepository;
import com.be.ebooki.repository.UserBookProgressRepository;
import com.be.ebooki.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    @Value("${app.base-url}")
    private String baseUrl;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamUserRepository teamUserRepository;
    private final UserBookProgressRepository userBookProgressRepository;

    private final RedisService redisService;

    private static final String INVITE_LINK_PREFIX = "invite:team:%d";


    @Transactional
    public TeamResponse.TeamInfoDTO initTeam(Integer userId, String teamName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다");
        }

        TeamResponse.TeamDTO teamDTO = createTeam(teamName); //팀 생성
        TeamResponse.TeamUserDTO teamUserDTO = joinTeamAndUser(userId, teamDTO); //팀 생성 후 유저 추가
        String inviteUrl = inviteTeam(userId, teamDTO); //그 후 링크 생성 일괄 처리 -> 트랜잭션 필요

        return TeamResponse.TeamInfoDTO.builder()
                .teamData(teamDTO)
                .teamUserData(teamUserDTO)
                .inviteUrl(inviteUrl)
                .build();
    }

    private TeamResponse.TeamDTO createTeam(String teamName) {

        Team team = teamRepository.save(Team.builder()
                .teamName(teamName)
                .build());

        return TeamResponse.TeamDTO.from(team);
    }

    public TeamResponse.TeamUserDTO joinTeamAndUser(Integer userId, TeamResponse.TeamDTO teamDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        Team team = teamRepository.findById(teamDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        if (team.getTeamName() == null || team.getTeamName().isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다");
        }

        //팀-유저 저장
        TeamUser teamUser = teamUserRepository.save(
                TeamUser.builder()
                        .user(user)
                        .team(team)
                        .build()
        );
        user.getTeamUsers().add(teamUser); //유저가 속한 팀을 조회하기 위해 추가

        return TeamResponse.TeamUserDTO.from(teamUser);
    }

    //copy link 할 때 생성되는 일회성 초대 링크
    public String inviteTeam(Integer userId, TeamResponse.TeamDTO teamDTO) {
        //외부에서 접근 가능하므로 유효성 검사 필요
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        Team team = teamRepository.findById(teamDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        if (team.getTeamName() == null || team.getTeamName().isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다");
        }

        //초대 링크를 만들어서 반환
        String key = INVITE_LINK_PREFIX.formatted(team.getId());
        String value = redisService.getValues(key); //value 불러오기

        //초대링크 만료 시 재발급
        if(value == null || value.equals("false")){
            final String randomCode = UUID.randomUUID().toString();
            redisService.setValues(key, randomCode, RedisService.expireTime());
            value = randomCode;
        }

        //초대링크 유효 시 그대로 return
        return baseUrl + "/teams/invite?token=" + value;
    }

    public Double calculateTeamRating(Integer teamId) {

        // 팀원 user list 가져오기
        List<Integer> memberIds = teamUserRepository.findUserIdsByTeamId(teamId);

        // 팀이 읽는 책
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        Integer bookId = team.getBook().getId();

        // 해당 유저들의 UserBookProgress 중 rating 있는 것만 조회
        List<Integer> ratings = userBookProgressRepository
                .findRatingsByUserIdsAndBook(memberIds, bookId); // rating != null인 것만

        if (ratings.isEmpty()) {
            return null; // 아직 아무도 평가 안함
        }

        return ratings.stream()
                .mapToInt(r -> r)
                .average()
                .orElse(0);
    }


}

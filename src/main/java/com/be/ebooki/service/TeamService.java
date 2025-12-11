package com.be.ebooki.service;

import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import com.be.ebooki.domain.User;
import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.repository.TeamRepository;
import com.be.ebooki.repository.TeamUserRepository;
import com.be.ebooki.repository.UserBookProgressRepository;
import com.be.ebooki.repository.UserRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Duration;

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

    private static final String INVITE_LOCKED_PREFIX = "lock:team:create:%s:%s:%d"; //초대링크 생성시 사용
    private static final String JOIN_LOCKED_PREFIX = "lock:team:join:%d";//초대링크 접속시 사용

    @Transactional
    public TeamResponse.TeamInfoDTO initTeam(Integer userId, String teamName, Integer bookId) {
        //클라이언트가 더블 클릭 시 중복 팀 생성 가능성 -> 멱등키 관리
        String lockedKey = INVITE_LOCKED_PREFIX.formatted(userId, teamName, bookId);
        boolean isLocked = redisService.setIfAbsent(lockedKey, "1", Duration.ofSeconds(1));

        if(!isLocked){
            throw new IllegalStateException("초대 링크 생성 중복 요청입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다");
        }

        TeamResponse.TeamDTO teamDTO = createTeam(teamName, bookId); //팀 생성
        TeamResponse.TeamUserDTO teamUserDTO = joinTeamAndUser(userId, teamDTO); //팀 생성 후 유저 추가

        List<TeamResponse.TeamUserDTO> teamUsersDTO = teamUserRepository.findAllByTeamId(teamDTO.getId())
                .stream()
                .map(TeamResponse.TeamUserDTO::from)
                .toList();
        String inviteUrl = inviteTeam(userId, teamDTO); //그 후 링크 생성 일괄 처리 -> 트랜잭션 필요

        return TeamResponse.TeamInfoDTO.builder()
                .teamData(teamDTO)
                .teamUserData(teamUsersDTO)
                .inviteUrl(inviteUrl)
                .build();
    }

    private TeamResponse.TeamDTO createTeam(String teamName, Integer bookId) {

        Team team = teamRepository.save(Team.builder()
                .teamName(teamName)
                .bookId(bookId)
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
        String inviteKey = INVITE_LINK_PREFIX.formatted(team.getId());
        String inviteValue = redisService.getValues(inviteKey); //value 불러오기

        //초대링크 만료 시 재발급
        if(inviteValue == null || inviteValue.equals("false")){
            final String randomCode = UUID.randomUUID().toString();
            redisService.setValues(inviteKey, randomCode, RedisService.expireTime());
            inviteValue = randomCode;
        }

        //초대링크 유효 시 그대로 return
        return baseUrl + "/api/teams/invite?token=" + inviteValue;
    }

    public TeamResponse.TeamInfoDTO getTeamInfo(String token) {
        Integer teamId = redisService.findByTeamByToken(token); //INVITE_LINK_PREFIX 로 만들어진 토큰 필요

        // 존재하지 않거나 만료된 링크면 예외 처리
        if (teamId == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 초대 링크입니다.");
        }

        // team 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // team에 속한 user 조회
        List<TeamResponse.TeamUserDTO> teamUsersDTO = teamUserRepository.findAllByTeamId(team.getId())
                .stream()
                .map(TeamResponse.TeamUserDTO::from)
                .toList();

        // DTO 변환 후 반환
        return TeamResponse.TeamInfoDTO.builder()
                .teamData(TeamResponse.TeamDTO.from(team))
                .teamUserData(teamUsersDTO)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TeamResponse.TeamInfoDTO acceptInvite(Integer userId, String token) {
        //팀 정보 검사
        TeamResponse.TeamInfoDTO teamInfoDTO = getTeamInfo(token);
        //이미 팀에 속해있는지 검사
        boolean alreadyJoined = teamUserRepository.existsByTeamIdAndUserId(teamInfoDTO.getTeamData().getId(), userId);

        if(alreadyJoined){
            throw new IllegalStateException("이미 팀에 속해 있는 사용자입니다.");
        }

        //분산 락
        String lockedKey = JOIN_LOCKED_PREFIX.formatted(teamInfoDTO.getTeamData().getId());
        boolean isLocked = redisService.setIfAbsent(lockedKey, userId.toString(), Duration.ofSeconds(5));

        if(!isLocked){
            throw new IllegalStateException("동시 가입 요청이 있습니다. 잠시 후 다시 시도해주세요.");
        }

        try{
            //회원 count
            long memberCount = teamUserRepository.countByTeamId(teamInfoDTO.getTeamData().getId());
            if(memberCount >= 4){
                throw new IllegalStateException("팀원은 4명까지 가능합니다.");
            }
            //팀 가입
            joinTeamAndUser(userId, teamInfoDTO.getTeamData());

            List<TeamResponse.TeamUserDTO> teamUserDTOS = teamUserRepository.findAllByTeamId(teamInfoDTO.getTeamData().getId())
                    .stream()
                    .map(TeamResponse.TeamUserDTO::from)
                    .toList();

            return TeamResponse.TeamInfoDTO.builder()
                    .teamData(teamInfoDTO.getTeamData())
                    .teamUserData(teamUserDTOS)
                    .inviteUrl(teamInfoDTO.getInviteUrl())
                    .build();

        }finally{
            redisService.delete(lockedKey);
        }
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

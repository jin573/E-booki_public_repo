package com.be.ebooki.service;

import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import com.be.ebooki.domain.User;
import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.UserBookProgress;
import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.enums.UserColor;
import com.be.ebooki.repository.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.Duration;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    @Value("${app.base-url}")
    private String baseUrl;
    private final UserPlanService userPlanService;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamUserRepository teamUserRepository;
    private final BookRepository bookRepository;
    private final UserBookProgressRepository userBookProgressRepository;
    private final RedisService redisService;


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

        //유효성 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        userPlanService.validateActivePlan(user); //요금제 조회
        Team team = createTeam(teamName, bookId); //팀 생성
        joinTeamAndUser(user.getId(), team.getId()); //팀 생성 후 유저 추가
        userPlanService.consumeOneBook(user);// 요금제 차감
        String inviteUrl = inviteTeam(userId, team.getId()); //초대 링크 생성
        return TeamResponse.TeamInfoDTO.builder()
                .teamData(TeamResponse.TeamDTO.from(team))
                .teamUserData(
                        teamUserRepository.findAllByTeamId(team.getId())
                        .stream()
                        .map(TeamResponse.TeamUserDTO::from)
                        .toList()
                )
                .inviteUrl(inviteUrl)
                .build();
    }

    private Team createTeam(String teamName, Integer bookId) {
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다."));

        return teamRepository.save(Team.builder()
                .teamName(teamName)
                .book(book)
                .build());
    }

    private void joinTeamAndUser(Integer userId, Integer teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        //유저 컬러 랜덤 생성
        Set<UserColor> userColorSet = teamUserRepository.findAllByTeamId(teamId)
                .stream()
                .map(TeamUser::getUserColor)
                .collect(Collectors.toSet());
        UserColor newColor = UserColor.randomColor(userColorSet);

        //팀-유저 저장
        TeamUser teamUser = teamUserRepository.save(
                TeamUser.builder()
                        .user(user)
                        .team(team)
                        .userColor(newColor)
                        .build()
        );
        user.getTeamUsers().add(teamUser); //유저가 속한 팀을 조회하기 위해 추가
    }

    //copy link 할 때 생성되는 일회성 초대 링크
    private String inviteTeam(Integer userId, Integer teamId) {
        //외부에서 접근 가능하므로 유효성 검사 필요 (나중에 public으로 변경)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        if (team.getTeamName() == null || team.getTeamName().isBlank()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다");
        }

        //기존 key 조회
        String existingKey = redisService.getValues("invite:team:" + team.getId());
        if(existingKey != null){
            return baseUrl + "/api/teams/invite?token=" + existingKey;
        }

        //없는 경우 새로 만들기
        String inviteKey = UUID.randomUUID().toString();
        redisService.setValues("invite:team:" + team.getId(), inviteKey, RedisService.expireTime()); //내부에서 사용
        redisService.setValues("invite:token:" + inviteKey, String.valueOf(team.getId()), RedisService.expireTime()); //랜덤 링크

        return baseUrl + "/api/teams/invite?token=" + inviteKey;
    }

    @Transactional
    public String reissueInvite(Integer userId, Integer teamId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        boolean isMember = teamUserRepository.existsByTeamIdAndUserId(teamId, userId);
        if (!isMember) {
            throw new IllegalStateException("팀원만 초대 링크를 재생성할 수 있습니다.");
        }

        if (team.getBook() == null) {
            throw new IllegalStateException("팀에 연결된 도서 정보가 없습니다.");
        }


        String oldToken = redisService.getValues("invite:team:" + teamId);
        if (oldToken != null) {
            redisService.delete("invite:token:" + oldToken);
            redisService.delete("invite:team:" + teamId);
        }

        String newToken = UUID.randomUUID().toString();
        redisService.setValues("invite:team:" + teamId, newToken, RedisService.expireTime());
        redisService.setValues("invite:token:" + newToken, String.valueOf(teamId), RedisService.expireTime());

        return baseUrl + "/api/teams/invite?token=" + newToken;
    }

    public TeamResponse.TeamInfoDTO getTeamInfo(String token) {
        String inviteToken = redisService.getValues("invite:token:"+token);

        // 존재하지 않거나 만료된 링크면 예외 처리
        if (inviteToken == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 초대 링크입니다.");
        }

        Integer teamId = Integer.parseInt(inviteToken);

        // team 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        // DTO 변환 후 반환
        return TeamResponse.TeamInfoDTO.builder()
                .teamData(TeamResponse.TeamDTO.from(team))
                .teamUserData(
                        teamUserRepository.findAllByTeamId(team.getId())
                                .stream()
                                .map(TeamResponse.TeamUserDTO::from)
                                .toList()
                )
                .build();
    }

    @Transactional
    public TeamResponse.TeamInfoDTO acceptInvite(Integer userId, String token) {
        //유효성 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        //팀 정보 검사
        TeamResponse.TeamInfoDTO teamInfoDTO = getTeamInfo(token);
        Integer teamId = teamInfoDTO.getTeamData().getId();
        //이미 팀에 속해있는지 검사
        boolean alreadyJoined = teamUserRepository.existsByTeamIdAndUserId(teamId, userId);
        if(alreadyJoined){
            throw new IllegalStateException("이미 팀에 속해 있는 사용자입니다.");
        }

        //분산 락
        String lockedKey = JOIN_LOCKED_PREFIX.formatted(teamId);
        boolean isLocked = redisService.setIfAbsent(lockedKey, userId.toString(), Duration.ofSeconds(5));
        if(!isLocked){
            throw new IllegalStateException("동시 가입 요청이 있습니다. 잠시 후 다시 시도해주세요.");
        }

        try{
            //회원 count
            long memberCount = teamUserRepository.countByTeamId(teamId);
            if(memberCount >= 4){
                redisService.delete("invite:token:" + token);
                redisService.delete("invite:team:" + teamId);
                throw new IllegalStateException("팀원은 4명까지 가능합니다.");
            }
            //요금제 조회
            userPlanService.validateActivePlan(user);
            //팀 가입

            joinTeamAndUser(userId, teamInfoDTO.getTeamData());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
            //독서 횟수 차감
            userPlanService.consumeOneBook(user);

            List<TeamResponse.TeamUserDTO> teamUserDTOS = teamUserRepository.findAllByTeamId(teamInfoDTO.getTeamData().getId())
                    .stream()
                    .map(TeamResponse.TeamUserDTO::from)
                    .toList();

            return TeamResponse.TeamInfoDTO.builder()
                    .teamData(teamInfoDTO.getTeamData())
                    .teamUserData(
                            teamUserRepository.findAllByTeamId(teamId)
                            .stream()
                            .map(TeamResponse.TeamUserDTO::from)
                            .toList()
                    )
                    .inviteUrl(teamInfoDTO.getInviteUrl())
                    .build();

        }finally{
            redisService.delete(lockedKey);
        }
    }

    public TeamResponse.TeamListResponse getMyTeams(Integer userId) {


        List<TeamUser> teamUsers = teamUserRepository.findAllByUserId(userId);

        List<Integer> teamIds = teamUsers.stream()
                .map(tu -> tu.getTeam().getId())
                .toList();

        List<Team> teams = teamRepository.findAllByIdIn(teamIds);
        List<TeamResponse.TeamListItemDTO> teamList = teams.stream()
                .map(team -> {

                    UserBookProgress progress = userBookProgressRepository
                            .findByUserIdAndBookId(userId, team.getBook().getId())
                            .orElse(null);

                    int percentage = progress != null
                            ? progress.getPercentage()
                            : 0;

                    boolean alreadyRated = progress != null && progress.getRating() != null;

                    boolean canRate = percentage == 100 && !alreadyRated;

                    Double avgRating = userBookProgressRepository
                            .findAverageRatingByBookId(team.getBook().getId());

                    return TeamResponse.TeamListItemDTO.builder()
                            .teamId(team.getId())
                            .teamName(team.getTeamName())
                            .bookTitle(team.getBook().getTitle())
                            .bookImage(team.getBook().getBookImage())
                            .memberProfileImages(
                                    teamUserRepository.findAllByTeamId(team.getId())
                                            .stream()
                                            .map(teamUser -> teamUser.getUser().getProfileImage())
                                            .toList()
                            )
                            .progressPercentage(percentage)
                            .canRate(canRate)
                            .averageRating(avgRating)
                            .build();

                })
                .toList();

        return TeamResponse.TeamListResponse.builder()
                .teams(teamList)
                .build();
    }

    @Transactional
    public void updateTeamName(Integer teamId, String teamName) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        team.updateTeamName(teamName);
    }

    public boolean validateMember(Integer teamId, Integer userId) {
        return teamUserRepository.existsByTeamIdAndUserId(teamId, userId);
    }

    public boolean validateTeamBook(Integer teamId, Integer bookId) {
        return teamRepository.existsByIdAndBook_Id(teamId, bookId);
    }
}
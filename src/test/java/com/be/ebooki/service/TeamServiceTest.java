package com.be.ebooki.service;

import com.be.ebooki.domain.*;
import com.be.ebooki.enums.UserColor;
import com.be.ebooki.enums.UserPlanStatus;
import com.be.ebooki.enums.UserType;
import com.be.ebooki.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
//@Transactional
public class TeamServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamUserRepository teamUserRepository;
    @Autowired
    private TeamService teamService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserPlanRepository userPlanRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clearAll() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        teamUserRepository.deleteAll();
        teamRepository.deleteAll();
        userPlanRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();
    }

    //팀, 조인 테이블, 초대 링크 성공
    @Test
    void testInitTeam_Success(){
        //fake user 생성
        User user = createFakeUser("test@example.com");
        createActivePlan(user, 10);

        //fake book 생성
        Book b = createFakeBook();

        var result = teamService.initTeam(user.getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                        .orElseThrow();

        //팀 생성 확인
        assertNotNull(result.getTeamData().getId(), "팀 id가 생성되어야 함");
        assertEquals("TestTeam", result.getTeamData().getTeamName());
        assertEquals(user.getId(), result.getTeamUserData().get(0).getUserId());
        //bookId 저장 확인
        assertEquals(result.getTeamData().getBookId(), savedTeam.getBook().getId());
        //teamuser 1명 저장 확인
        assertEquals(1, teamUserRepository.count(), "TeamUser 1명이어야 함");
        //redis 초대링크 확인
        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);

        assertNotNull(result.getInviteUrl(), "초대 링크가 생성되어야 함");
        assertNotNull(token, "redis에 초대 토큰이 저장되어야 함");
        assertFalse(token.isEmpty());
    }

    //user 없을 때
    @Test
    void testInitTeam_UserNotFound(){
        //존재하지 않는 유저
        Integer fakeUserId = 999;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
        {teamService.initTeam(fakeUserId, "FailTeam", 1);});

        assertTrue(exception.getMessage().contains("존재하지 않는 계정"));

    }

    //팀 생성 시 롤백
    @Test
    void testInitTeam_TeamNotFound(){
        User user = createFakeUser("test@example.com");

        Exception exception = assertThrows(Exception.class, () ->
        {teamService.initTeam(user.getId(), null, 1);});

        assertEquals(1, userRepository.count(), "User만 존재, Team은 저장되지 않아야 함");
    }

    //같은 유저가 같은 팀 이름으로 요청 시 두번째 실패 ->성공
    @Test
    void testLockedTeam_Success() throws InterruptedException{
        //유저 생성
        User user = createFakeUser("test@example.com");
        createActivePlan(user, 10);
        //fake book 생성
        Book b = createFakeBook();

        var result = teamService.initTeam(user.getId(), "TestTeam", b.getId());

        assertThrows(IllegalStateException.class, ()->
        {teamService.initTeam(user.getId(), "TestTeam", b.getId());});

        Thread.sleep(1100);

        assertDoesNotThrow(() -> teamService.initTeam(user.getId(), "TestTeam", b.getId()));

    }

    //다른 유저는 동일한 이름으로 팀 생성 가능
    @Test
    void testCreateTeamAsSameName_Success(){
        //유저 생성
        User user_1 = createFakeUser("test@example.com");
        User user_2 = createFakeUser("testtest@example.com");
        createActivePlan(user_1, 10);
        createActivePlan(user_2, 10);
        //fake book 생성
        Book b = createFakeBook();

        var result_1 = teamService.initTeam(user_1.getId(), "TestTeam", b.getId());
        var result_2 = teamService.initTeam(user_2.getId(), "TestTeam", b.getId());

        assertNotNull(result_1);
        assertNotNull(result_2);
    }

    //초대 링크로 접속해서 가입하기
    @Test
    void testJoinTeam_Success(){
        //fakeUser 생성
        List<User> userList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            User user = createFakeUser("test" + i + "@example.com");
            createActivePlan(user, 10);
            userList.add(user);
        }
        //fake book 생성
        Book b = createFakeBook();

        //유저 1은 팀 생성자
        var result = teamService.initTeam(userList.get(0).getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                .orElseThrow();

        //팀 생성 확인
        assertNotNull(result.getTeamData().getId(), "팀 id가 생성되어야 함");
        assertEquals("TestTeam", result.getTeamData().getTeamName());
        assertEquals(userList.get(0).getId(), result.getTeamUserData().get(0).getUserId());

        //유저 2, 3은 접속자
        //redis 토큰 가져오기
        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);
        teamService.acceptInvite(userList.get(1).getId(), token);
        teamService.acceptInvite(userList.get(2).getId(), token);

        List<TeamUser> teamUserList = teamUserRepository.findAllByTeamId(savedTeam.getId());
        //팀에 속해있는지 확인
        assertEquals(3, teamUserRepository.countByTeamId(savedTeam.getId()));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(userList.get(0).getId())));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(userList.get(1).getId())));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(userList.get(2).getId())));

        //유저 4가 접속 시 올바르게 되는지 확인
        teamService.acceptInvite(userList.get(3).getId(), token);
        teamUserList = teamUserRepository.findAllByTeamId(savedTeam.getId());
        assertEquals(4, teamUserRepository.countByTeamId(savedTeam.getId()));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(userList.get(3).getId())));

        //유저 5가 접속시 인원 count 되는지 확인
        Exception exception = assertThrows(IllegalStateException.class, ()->
        {teamService.acceptInvite(userList.get(4).getId(), token);});

        assertTrue(exception.getMessage().contains("팀원은 4명까지 가능합니다."));

    }

    //회원이 아닌 유저 가입 시 거절
    @Test
    void testJoinTeam_UserNotFound(){
        //유저, 책, 팀 생성
        User user = createFakeUser("test@example.com");
        createActivePlan(user, 10);

        Book b = createFakeBook();

        var result = teamService.initTeam(user.getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                .orElseThrow();

        //토큰 가져오기
        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);

        //존재하지 않는 유저
        Integer fakeUserId = 999;

        Exception exception = assertThrows(IllegalArgumentException.class, ()->
        {teamService.acceptInvite(fakeUserId, token);});

        assertTrue(exception.getMessage().contains("존재하지 않는 계정입니다."));

    }

    //요금제 없을 시 거절
    @Test
    void testJoinTeam_NoActivePlan() {
        User user_1 = createFakeUser("test1@example.com");
        User user_2 = createFakeUser("test2@example.com");

        createActivePlan(user_1, 10); // 팀 생성자만 있음

        Book b = createFakeBook();

        var result = teamService.initTeam(user_1.getId(), "TestTeam", b.getId());

        String token = redisService.getValues("invite:team:" + result.getTeamData().getId());

        Exception exception = assertThrows(IllegalStateException.class, () ->
                teamService.acceptInvite(user_2.getId(), token)
        );

        assertTrue(exception.getMessage().contains("사용 가능한 요금제가 없습니다."));
    }

    //이미 가입한 사용자일 경우 거절
    @Test
    void testJoinTeam_AlreadyJoined(){
        //유저, 책, 팀 생성
        User user = createFakeUser("test@example.com");
        createActivePlan(user, 10);

        Book b = createFakeBook();

        var result = teamService.initTeam(user.getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                .orElseThrow();

        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);
        System.out.println(token);

        //재가입
        Exception exception = assertThrows(IllegalStateException.class, ()->
        {teamService.acceptInvite(user.getId(), token);});

        assertTrue(exception.getMessage().contains("이미 팀에 속해 있는 사용자입니다."));

    }

    //동시 요청 시 접속 어떻게 되는지 테스트
    @Test
    void testJoinTeam_Concurrency() throws InterruptedException, ExecutionException {
        //팀 생성 유저
        User createUser = createFakeUser("test@example.com");
        createActivePlan(createUser, 10);

        Book b = createFakeBook();
        var result = teamService.initTeam(createUser.getId(), "TestTeam", b.getId());
        String token = redisService.getValues("invite:team:" + result.getTeamData().getId());

        //fakeUser 생성
        List<User> userList = new ArrayList<>();
        for (int i = 2; i <= 5; i++) {
            User user = createFakeUser("test" + i + "@example.com");
            createActivePlan(user, 10);
            userList.add(user);
        }

        //스레드 생성
        ExecutorService executorService = Executors.newFixedThreadPool(userList.size());
        //결과 저장
        List<Future<String>> futures = new ArrayList<>();

        for (User u : userList) {
            futures.add(executorService.submit(() -> {
                int retries = 3;
                while(retries-- > 0){
                    try {
                        teamService.acceptInvite(u.getId(), token);
                        return u.getEmail() + " 가입 성공했습니다.";
                    } catch (Exception e) {
                        if (e.getMessage().contains("동시 가입 요청")) {
                            Thread.sleep(50); // 짧게 대기 후 재시도
                        } else {
                            return u.getEmail() + " 가입 실패: " + e.getMessage();
                        }
                    }
                }
                return u.getEmail() + "가입 실패: 락 획득 실패";
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 결과 출력
        int successCount = 0;
        for (Future<String> f : futures) {
            String str = f.get();
            System.out.println(str);
            if(str.contains("성공")) successCount++;

        }

        assertTrue(successCount <= 3, "3명 이하만 가입 성공해야 함");
        long finalCount = teamUserRepository.countByTeamId(result.getTeamData().getId());
        assertEquals(4, finalCount, "최종 팀원 수는 4명");
    }

    //요금제 차감
    @Test
    @Transactional
    void testPlanConsumedAfterJoin() {
        User user_1 = createFakeUser("test1@example.com");
        User user_2 = createFakeUser("test2@example.com");

        createActivePlan(user_1, 10);
        createActivePlan(user_2, 1);

        Book b = createFakeBook();

        var result = teamService.initTeam(user_1.getId(), "TestTeam", b.getId());
        String token = redisService.getValues("invite:team:" + result.getTeamData().getId());

        teamService.acceptInvite(user_2.getId(), token);

        UserPlan plan = userPlanRepository
                .findByUserAndStatus(user_2, UserPlanStatus.EXPIRED)
                .orElseThrow();

        assertEquals(1, plan.getUsedBookCount());
    }

    //컬러 랜덤 부여
    @Test
    void testRandomColor_Success(){
        //team 생성
        Team team = teamRepository.save(new Team("testTeam", null));
        //fakeUser 생성
        List<User> userList = new ArrayList<>();
        for (int i = 0; i <4; i++) {
            User user = createFakeUser("test" + i + "@example.com");
            createActivePlan(user, 10);
            userList.add(user);
        }
        //color set
        Set<UserColor> ColorSet = new HashSet<>();

        // 팀원 가입 & 컬러 배정
        for (User user : userList) {
            Set<UserColor> usedColors = teamUserRepository.findAllByTeamId(team.getId())
                    .stream()
                    .map(TeamUser::getUserColor)
                    .collect(Collectors.toSet());

            UserColor newColor = UserColor.randomColor(usedColors);

            TeamUser teamUser = teamUserRepository.save(
                    TeamUser.builder()
                            .team(team)
                            .user(user)
                            .userColor(newColor)
                            .build()
            );

            ColorSet.add(teamUser.getUserColor());
        }

        //팀원 수 확인
        assertEquals(4, teamUserRepository.countByTeamId(team.getId()));

        //컬러 중복 없음 확인
        assertEquals(4, ColorSet.size());

        //Enum 컬러만 사용됐는지 확인
        for (UserColor color : ColorSet) {
            assertTrue(Arrays.asList(UserColor.values()).contains(color));
        }

    }
    User createFakeUser(String email){
        return userRepository.save(
                User.builder()
                        .email(email)
                        .nickname(email + " nickname")
                        .userType(UserType.LOCAL)
                        .build()
        );
    }

    Book createFakeBook(){
        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);
        bookRepository.save(b);
        return b;
    }

    void createActivePlan(User user, int totalCount){
        userPlanRepository.save(
                UserPlan.builder()
                        .user(user)
                        .totalBookCount(totalCount)
                        .build()
        );
    }

}

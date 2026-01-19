package com.be.ebooki.service;

import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import com.be.ebooki.domain.User;
import com.be.ebooki.enums.UserType;
import com.be.ebooki.repository.BookRepository;
import com.be.ebooki.repository.TeamRepository;
import com.be.ebooki.repository.TeamUserRepository;
import com.be.ebooki.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

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

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @BeforeEach
    void clearDatabase() {
        teamUserRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();
        bookRepository.deleteAll();
    }

    //팀, 조인 테이블, 초대 링크 성공
    @Test
    void testInitTeam_Success(){
        //fake user 생성
        User user = createFakeUser("test@example.com");

        //fake book 생성
        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);

        bookRepository.save(b);

        var result = teamService.initTeam(user.getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                        .orElseThrow();

        //팀 생성 확인
        assertNotNull(result.getTeamData().getId(), "팀 id가 생성되어야 함");
        assertEquals("TestTeam", result.getTeamData().getTeamName());
        assertEquals(user.getId(), result.getTeamUserData().get(0).getUserId());
        //bookId 저장 확인
        assertEquals(result.getTeamData().getBookId(), savedTeam.getBookId());
        //teamuser 1명 저장 확인
        assertEquals(1, teamUserRepository.count(), "TeamUser 1명이어야 함");
        //redis 초대링크 확인
        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);

        assertNotNull(result.getInviteUrl(), "초대 링크가 생성되어야 함");
        assertNotNull(token, "redis에 초대 토큰이 저장되어야 함");
        assertFalse(token.isEmpty());
    }
    //셋 중 하나 실패 -> 트랜잭션 테스트
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
        //fake book 생성
        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);

        bookRepository.save(b);
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
        //fake book 생성
        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);

        bookRepository.save(b);

        var result_1 = teamService.initTeam(user_1.getId(), "TestTeam", b.getId());
        var result_2 = teamService.initTeam(user_2.getId(), "TestTeam", b.getId());

        assertNotNull(result_1);
        assertNotNull(result_2);
    }
    //초대 링크로 접속해서 가입하기
    @Test
    void testJoinTeam_Success(){
        //fake user 생성
        User user_1 = createFakeUser("test1@example.com");
        User user_2 = createFakeUser("test2@example.com");
        User user_3 = createFakeUser("test3@example.com");
        User user_4 = createFakeUser("test4@example.com");
        User user_5 = createFakeUser("test5@example.com");

        //fake book 생성
        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);

        bookRepository.save(b);

        //유저 1은 팀 생성자
        var result = teamService.initTeam(user_1.getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                .orElseThrow();

        //팀 생성 확인
        assertNotNull(result.getTeamData().getId(), "팀 id가 생성되어야 함");
        assertEquals("TestTeam", result.getTeamData().getTeamName());
        assertEquals(user_1.getId(), result.getTeamUserData().get(0).getUserId());

        //유저 2, 3은 접속자
        //redis 토큰 가져오기
        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);
        teamService.acceptInvite(user_2.getId(), token);
        teamService.acceptInvite(user_3.getId(), token);

        List<TeamUser> teamUserList = teamUserRepository.findAllByTeamId(savedTeam.getId());
        //팀에 속해있는지 확인
        assertEquals(3, teamUserRepository.countByTeamId(savedTeam.getId()));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(user_1.getId())));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(user_2.getId())));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(user_3.getId())));

        //유저 4가 접속 시 올바르게 되는지 확인
        teamService.acceptInvite(user_4.getId(), token);
        teamUserList = teamUserRepository.findAllByTeamId(savedTeam.getId());
        assertEquals(4, teamUserRepository.countByTeamId(savedTeam.getId()));
        assertTrue(teamUserList.stream()
                .anyMatch(teamUser -> teamUser.getUser().getId().equals(user_4.getId())));

        //유저 5가 접속시 인원 count 되는지 확인
        Exception exception = assertThrows(IllegalStateException.class, ()->
        {teamService.acceptInvite(user_5.getId(), token);});

        assertTrue(exception.getMessage().contains("팀원은 4명까지 가능합니다."));


    }
    //회원이 아닌 유저 가입 시 거절
    @Test
    void testJoinTeam_UserNotFound(){
        //유저, 책, 팀 생성
        User user = createFakeUser("test@example.com");

        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);
        bookRepository.save(b);

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

    //이미 가입한 사용자일 경우 거절
    @Test
    void testJoinTeam_AlreadyJoined(){
        //유저, 책, 팀 생성
        User user = createFakeUser("test@example.com");

        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);
        bookRepository.save(b);

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
    //15분 지난 링크로 접속 시 거절

    //동시 요청 시 접속 어떻게 되는지 테스트
    @Test
    void testJoinTeam_Concurrency() throws InterruptedException, ExecutionException {
        //팀 생성 유저
        User createUser = createFakeUser("test@example.com");

        Book b = new Book();
        b.setTitle("fake book");
        b.setAuthor("fake author");
        b.setPublisher("fake publisher");
        b.setPrice(10000);
        b.setBookImage("fake url");
        b.setRating(4.8);
        bookRepository.save(b);

        var result = teamService.initTeam(createUser.getId(), "TestTeam", b.getId());
        Team savedTeam = teamRepository.findById(result.getTeamData().getId())
                .orElseThrow();

        teamRepository.flush();

        String redisKey = "invite:team:" + savedTeam.getId();
        String token = redisService.getValues(redisKey);

        //fakeUser 생성
        User user_2 = createFakeUser("test2@example.com");
        User user_3 = createFakeUser("test3@example.com");
        User user_4 = createFakeUser("test4@example.com");
        User user_5 = createFakeUser("test5@example.com");

        List<User> userList = List.of(user_2, user_3, user_4, user_5);

        //스레드 생성
        ExecutorService executorService = Executors.newFixedThreadPool(userList.size());
        //결과 저장
        List<Future<String>> futures = new ArrayList<>();

        for (User u : userList) {
            futures.add(executorService.submit(() -> {
                try {
                    teamService.acceptInvite(u.getId(), token);
                    return u.getEmail() + " 가입 성공했습니다.";
                } catch (Exception e) {
                    return u.getEmail() + " 가입 실패: " + e.getMessage();
                }
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // 결과 출력
        for (Future<String> f : futures) {
            System.out.println(f.get());
        }

        //잠깐 대기 후 재 접속
        for (User u : userList) {
            try {
                teamService.acceptInvite(u.getId(), token);
                System.out.println( u.getEmail() + " 가입 성공했습니다.");
            } catch (Exception e) {
                System.out.println(u.getEmail() + " 가입 실패: " + e.getMessage());
            }
        }
        // 최종 팀원 수 확인 (팀원 최대 4명)
        long count = teamUserRepository.countByTeamId(savedTeam.getId());
        System.out.println("최종 팀원 수: " + count);
        assertTrue(count <= 4, "팀원 최대 4명 제한 확인");
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
}

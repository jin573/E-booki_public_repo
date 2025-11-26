package com.be.ebooki.service;

import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.Team;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
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

    //팀, 조인 테이블, 초대 링크 성공
    @Test
    void testInitTeam_Success(){
        //fake user 생성
        User user = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .nickname("testUser")
                        .userType(UserType.LOCAL)
                        .build()
        );

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
        User user = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .nickname("testUser")
                        .userType(UserType.LOCAL)
                        .build()
        );

        Exception exception = assertThrows(Exception.class, () ->
        {teamService.initTeam(user.getId(), null, 1);});

        assertEquals(1, userRepository.count(), "User만 존재, Team은 저장되지 않아야 함");
    }

    //같은 유저가 같은 팀 이름으로 요청 시 두번째 실패 ->성공
    @Test
    void testLockedTeam_Success() throws InterruptedException{
        //유저 생성
        User user = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .nickname("testUser")
                        .userType(UserType.LOCAL)
                        .build()
        );
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
        User user_1 = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .nickname("testUser1")
                        .userType(UserType.LOCAL)
                        .build()
        );

        User user_2 = userRepository.save(
                User.builder()
                        .email("testtest@example.com")
                        .nickname("testUser2")
                        .userType(UserType.LOCAL)
                        .build()
        );
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
}

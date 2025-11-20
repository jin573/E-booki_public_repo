package com.be.ebooki.controller;

import com.be.ebooki.domain.Team;
import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.service.TeamService;
import com.be.ebooki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    //팀 로직은 다음과 같다
    //선행 로직 : 결제 ? (의논 필요)
    // 1. "같이 읽으러 가기" 버튼 클릭
    // 2. 팀 초대 페이지에서 이름 입력
    // 3. copy link 눌러야 팀 db 생성

    private final TeamService teamService;

    private final UserService userService;

    @PostMapping("/{teamName}")
    public ResponseEntity<?> createTeam(@PathVariable String teamName){

        Integer userId = userService.getCurrentUserId();

        TeamResponse.TeamInfoDTO teamInfoDTO = teamService.initTeam(userId, teamName);
        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO>builder()
                .statusCode(200)
                .message("팀 생성 성공 및 링크 생성 성공")
                .data(teamInfoDTO)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

}

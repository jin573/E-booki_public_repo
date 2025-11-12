package com.be.ebooki.controller;

import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.service.TeamService;
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

    @PostMapping("/{teamName}")
    public ResponseEntity<?> createTeam(@PathVariable String teamName){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal(); // 로그인한 유저 이메일
        System.out.println(email);

        TeamResponse.TeamDTO teamDTO = teamService.createTeam(email, teamName);
        TeamResponse.TeamUserDTO teamUserDTO = teamService.initTeam(email, teamDTO);

        TeamResponse.TeamResponseDTO<TeamResponse.TeamDTO, TeamResponse.TeamUserDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamDTO, TeamResponse.TeamUserDTO>builder()
                .statusCode(200)
                .message("팀 생성 성공")
                .teamData(teamDTO)
                .teamUserData(teamUserDTO)
                .build();

        return ResponseEntity.ok(responseDTO);
    }

}

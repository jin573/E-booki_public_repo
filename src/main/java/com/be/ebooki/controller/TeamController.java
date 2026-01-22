package com.be.ebooki.controller;


import com.be.ebooki.domain.User;
import com.be.ebooki.dto.TeamRequest;
import com.be.ebooki.dto.BookResponse;

import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.repository.UserRepository;
import com.be.ebooki.service.BookService;
import com.be.ebooki.service.TeamService;
import com.be.ebooki.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final BookService bookService;


    @PostMapping
    @Operation(
            summary = "팀 생성"
    )
    public ResponseEntity<?> createTeam(@RequestBody TeamRequest.TeamInitDTO teamInitDTO){
        Integer userId = userService.getCurrentUserId();

        TeamResponse.TeamInfoDTO teamInfoDTO = teamService.initTeam(userId, teamInitDTO.getTeamName(), teamInitDTO.getBookId());
        BookResponse.BookDetailDTO bookDTO = bookService.getBookDetail(teamInfoDTO.getTeamData().getBookId(), userId);
        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookDetailDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO, BookResponse.BookDetailDTO>builder()
                .statusCode(200)
                .message("팀 생성 성공 및 링크 생성 성공")
                .teamData(teamInfoDTO)
                .bookData(bookDTO)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/invite")
    @Operation(
            summary = "초대 링크 접속"
    )
    public ResponseEntity<?> inviteUser(@RequestParam String token){
        //누구나 접속 가능
        //수락 버튼 눌렀을 경우 요금제 검사, user 유효성 검사 진행
        //현재 api에서는 팀 정보, 도서 정보, 팀원 리스트를 가져와야 한다.

        TeamResponse.TeamInfoDTO teamInfoDTO = teamService.getTeamInfo(token);
        BookResponse.BookPreviewDTO bookPreviewDTO = bookService.getBookPreview(teamInfoDTO.getTeamData().getBookId());

        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO>builder()
                .statusCode(200)
                .message("팀, 도서, 팀원 정보 불러오기 성공")
                .teamData(teamInfoDTO)
                .bookData(bookPreviewDTO)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/invite/join")
    @Operation(
            summary = "초대 링크 수락"
    )
    public ResponseEntity<?> joinUser(@RequestParam String token){
        //user 검사
        Integer userId = userService.getCurrentUserId();
        //팀 검사
        TeamResponse.TeamInfoDTO teamInfoDTO = teamService.acceptInvite(userId, token); //팀에 올바르게 추가 된 경우
        BookResponse.BookDetailDTO bookDTO = bookService.getBookDetail(teamInfoDTO.getTeamData().getBookId(), userId); //도서를 가져와서 추가하기

        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookDetailDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO, BookResponse.BookDetailDTO>builder()
                .statusCode(200)
                .message("팀원 추가 성공")
                .teamData(teamInfoDTO)
                .bookData(bookDTO)
                .build();

        return ResponseEntity.ok(responseDTO);

    }

    @GetMapping
    public ResponseEntity<TeamResponse.TeamListResponse> getMyTeams() {
        Integer userId = userService.getCurrentUserId();

        TeamResponse.TeamListResponse response =
                teamService.getMyTeams(userId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{teamId}")
    public ResponseEntity<Void> updateTeamName(
            @PathVariable Integer teamId,
            @RequestBody TeamRequest.UpdateTeamName request
    ) {
        teamService.updateTeamName(teamId, request.getTeamName());

        return ResponseEntity.ok().build();
    }

}

package com.be.ebooki.controller;

import com.be.ebooki.dto.TeamRequest;
import com.be.ebooki.dto.BookResponse;
import com.be.ebooki.dto.TeamResponse;
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
        BookResponse.BookPreviewDTO bookPreviewDTO =bookService.getBookPreview(teamInfoDTO.getTeamData().getBookId());

        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO>builder()
                .statusCode(200)
                .message("팀 생성 성공 및 링크 생성 성공")
                .teamData(teamInfoDTO)
                .bookData(bookPreviewDTO)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    /**누구나 접속 가능*/
    @GetMapping("/invite")
    @Operation(
            summary = "초대 링크 접속"
    )
    public ResponseEntity<?> inviteUser(@RequestParam String token){

        TeamResponse.TeamInfoDTO teamInfoDTO = teamService.getTeamInfoByToken(token);
        BookResponse.BookPreviewDTO bookPreviewDTO =bookService.getBookPreview(teamInfoDTO.getTeamData().getBookId());

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
        BookResponse.BookPreviewDTO bookPreviewDTO =bookService.getBookPreview(teamInfoDTO.getTeamData().getBookId()); //도서를 가져와서 추가하기

        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO>builder()
                .statusCode(200)
                .message("팀원 추가 성공")
                .teamData(teamInfoDTO)
                .bookData(bookPreviewDTO)
                .build();

        return ResponseEntity.ok(responseDTO);

    }

    @PostMapping("/invite/reissue")
    @Operation(
            summary = "초대 링크 재생성"
    )
    public ResponseEntity<?> reissueInvite(@RequestParam Integer teamId){

        //user 검사
        Integer userId= userService.getCurrentUserId();
        String newURL = teamService.reissueInvite(userId, teamId);

        TeamResponse.TeamResponseDTO<TeamResponse.ReissueTeamUrlResponse, Void> responseDTO = TeamResponse.TeamResponseDTO.<TeamResponse.ReissueTeamUrlResponse, Void>builder()
                .statusCode(200)
                .message("초대 링크 재생성 성공")
                .teamData(TeamResponse.ReissueTeamUrlResponse.builder()
                        .teamId(teamId)
                        .newUrl(newURL).build())
                .bookData(null)
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/list")
    public ResponseEntity<TeamResponse.TeamListResponse> getMyTeams() {
        Integer userId = userService.getCurrentUserId();

        TeamResponse.TeamListResponse response =
                teamService.getMyTeams(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}")
    @Operation(
            summary = "팀 상세 조회"
    )
    public ResponseEntity<TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO>> getTeam(@PathVariable Integer teamId) {
        Integer userId = userService.getCurrentUserId();
        TeamResponse.TeamInfoDTO teamInfoDTO = teamService.getTeamInfo(teamId, userId);
        BookResponse.BookPreviewDTO bookPreviewDTO =bookService.getBookPreview(teamInfoDTO.getTeamData().getBookId());

        TeamResponse.TeamResponseDTO<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO> responseDTO
                = TeamResponse.TeamResponseDTO.<TeamResponse.TeamInfoDTO, BookResponse.BookPreviewDTO>builder()
                .statusCode(200)
                .message("팀 상세 정보 가져오기 성공")
                .teamData(teamInfoDTO)
                .bookData(bookPreviewDTO)
                .build();

        return ResponseEntity.ok(responseDTO);
    }
    @PutMapping("/{teamId}")
    public ResponseEntity<Void> updateTeamName(
            @PathVariable Integer teamId,
            @RequestBody TeamRequest.UpdateTeamName request
    ) {
        Integer userId = userService.getCurrentUserId();

        teamService.updateTeamName(teamId, request.getTeamName());

        return ResponseEntity.ok().build();
    }
}

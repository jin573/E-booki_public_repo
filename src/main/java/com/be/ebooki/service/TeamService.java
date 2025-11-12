package com.be.ebooki.service;

import com.be.ebooki.domain.Team;
import com.be.ebooki.domain.TeamUser;
import com.be.ebooki.domain.User;
import com.be.ebooki.dto.TeamResponse;
import com.be.ebooki.repository.TeamRepository;
import com.be.ebooki.repository.TeamUserRepository;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamUserRepository teamUserRepository;

    public TeamResponse.TeamDTO createTeam(String email, String teamName) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        //팀 우선 생성 후
        Team team = teamRepository.save(Team.builder()
                .teamName(teamName)
                .build());

        return TeamResponse.TeamDTO.from(team);
    }

    public TeamResponse.TeamUserDTO initTeam(String email, TeamResponse.TeamDTO teamDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        Team team = teamRepository.findById(teamDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀입니다."));

        //팀-유저 저장
        TeamUser teamUser = teamUserRepository.save(
                TeamUser.builder()
                        .user(user)
                        .team(team)
                        .build()
        );

        return TeamResponse.TeamUserDTO.from(teamUser);
    }
}

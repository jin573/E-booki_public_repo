package com.be.ebooki.repository;

import com.be.ebooki.domain.TeamUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamUserRepository extends JpaRepository<TeamUser, Integer> {
    boolean exsitsByUserIdAndTeamId(Integer userId, Integer teamId);
}

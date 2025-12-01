package com.be.ebooki.repository;

import com.be.ebooki.domain.TeamUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamUserRepository extends JpaRepository<TeamUser, Integer> {
    List<TeamUser> findAllByTeamId(Integer teamId);

    boolean existsByTeamIdAndUserId(Integer teamId, Integer userId);

    long countByTeamId(Integer teamId);
}

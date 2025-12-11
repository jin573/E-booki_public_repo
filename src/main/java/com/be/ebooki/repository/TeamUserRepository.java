package com.be.ebooki.repository;

import com.be.ebooki.domain.TeamUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamUserRepository extends JpaRepository<TeamUser, Integer>
{
    @Query("""
        SELECT tu.user.id
        FROM TeamUser tu
        WHERE tu.team.id = :teamId
    """)
    List<Integer> findUserIdsByTeamId(Integer teamId);

public interface TeamUserRepository extends JpaRepository<TeamUser, Integer> {
    List<TeamUser> findAllByTeamId(Integer teamId);

    boolean existsByTeamIdAndUserId(Integer teamId, Integer userId);

    long countByTeamId(Integer teamId);
}

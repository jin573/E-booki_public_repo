package com.be.ebooki.repository;

import com.be.ebooki.domain.TeamUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamUserRepository extends JpaRepository<TeamUser, Integer> {
    
    List<TeamUser> findAllByTeamId(Integer teamId);
    List<TeamUser> findAllByUserId(Integer userId);
    boolean existsByTeamIdAndUserId(Integer teamId, Integer userId);

    Optional<TeamUser> findByTeam_IdAndUser_Id(Integer teamId, Integer userId);
    long countByTeamId(Integer teamId);
}

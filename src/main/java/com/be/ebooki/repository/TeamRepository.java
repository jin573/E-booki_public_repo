package com.be.ebooki.repository;

import com.be.ebooki.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    List<Team> findAllByUserId(Integer userId);

    boolean existsByIdAndBook_Id(Integer teamId, Integer bookId);
}

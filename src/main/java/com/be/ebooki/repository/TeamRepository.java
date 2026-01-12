package com.be.ebooki.repository;

import com.be.ebooki.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    boolean existsByIdAndBook_Id(Integer teamId, Integer bookId);
}

package com.be.ebooki.repository;

import com.be.ebooki.domain.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightRepository extends JpaRepository<Highlight, Integer> {
    List<Highlight> findAllByBookId(Integer bookId);
    List<Highlight> findByBookIdAndTeamId(Integer bookId, Integer teamId);
    List<Highlight> findAllByBookIdAndTeamId(Integer bookId, Integer teamId);

}

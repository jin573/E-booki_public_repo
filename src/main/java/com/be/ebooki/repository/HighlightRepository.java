package com.be.ebooki.repository;

import com.be.ebooki.domain.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighlightRepository extends JpaRepository<Highlight, Integer> {
    List<Highlight> findByBookId(Integer bookId);
    List<Highlight> findByBookIdAndUserId(Integer bookId, Integer userId);

}

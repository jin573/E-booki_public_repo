package com.be.ebooki.repository;

import com.be.ebooki.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByHighlightId(Integer highlightId);
}

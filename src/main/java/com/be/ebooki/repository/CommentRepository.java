package com.be.ebooki.repository;

import com.be.ebooki.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByHighlightId(Integer highlightId);
    List<Comment> findByUserIdAndHighlightIdIn(Integer userId, List<Integer> highlightIds);
    @Query("""
    select c
    from Comment c
    where c.highlightId in (
        select h.id
        from Highlight h
        where h.bookId = :bookId
    )
""")
    List<Comment> findAllByBookId(@Param("bookId") Integer bookId);

    void deleteAllByHighlightId(Integer highlightId);
}

package com.be.ebooki.repository;

import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.Like;
import com.be.ebooki.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByUserAndBook(User user, Book book);
    boolean existsByUserIdAndBookId(Integer userId, Integer bookId);
}

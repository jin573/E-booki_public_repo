package com.be.ebooki.repository;

import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.User;
import com.be.ebooki.domain.UserBookProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserBookProgressRepository
        extends JpaRepository<UserBookProgress, Integer> {

    @Query("""
    select avg(ubp.rating)
    from UserBookProgress ubp
    where ubp.book.id = :bookId
      and ubp.rating is not null
""")
    Double findAverageRatingByBookId(@Param("bookId") Integer bookId);

    boolean existsByUserAndBook(User user, Book book);

    List<UserBookProgress> findByUserIdAndPercent(Integer userId, Double percent);

    Optional<UserBookProgress> findByUserAndBook(User user, Book book);
    Optional<UserBookProgress> findByUser_IdAndBook_Id(Integer userId, Integer bookId);
}

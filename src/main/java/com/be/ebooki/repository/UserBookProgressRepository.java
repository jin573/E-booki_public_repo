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

    //1) 나의 책 rating 조회
    @Query("""
            SELECT ubp.rating 
            FROM UserBookProgress ubp
            WHERE ubp.user.id = :userId
              AND ubp.book.id = :bookId
            """)
    Optional<Integer> findMyRating(Integer userId, Integer bookId);


    //2) 팀원들의 rating 목록 조회
    @Query("""
            SELECT ubp.rating
            FROM UserBookProgress ubp
            WHERE ubp.user.id IN :userIds
              AND ubp.book.id = :bookId
              AND ubp.rating IS NOT NULL
            """)
    List<Integer> findRatingsByUserIdsAndBook(List<Integer> userIds, Integer bookId);


    Optional<UserBookProgress> findByUserIdAndBookId(
            Integer userId,
            Integer bookId
    );

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

}

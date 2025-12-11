package com.be.ebooki.repository;

import com.be.ebooki.domain.UserBookProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}

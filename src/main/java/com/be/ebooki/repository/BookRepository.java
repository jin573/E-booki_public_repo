package com.be.ebooki.repository;

import com.be.ebooki.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query(
            value = """
    SELECT b.*
    FROM book b
    WHERE
        b.title COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
        OR b.author COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
        OR b.publisher COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
    ORDER BY
        CASE
            WHEN b.title COLLATE utf8mb4_general_ci LIKE CONCAT(:q, '%') THEN 3
            WHEN b.title COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%') THEN 2
            ELSE 1
        END DESC,
        b.id DESC
    """,
            countQuery = """
    SELECT COUNT(*)
    FROM book b
    WHERE
        b.title COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
        OR b.author COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
        OR b.publisher COLLATE utf8mb4_general_ci LIKE CONCAT('%', :q, '%')
    """,
            nativeQuery = true
    )
    Page<Book> searchBooks(
            @Param("q") String q,
            Pageable pageable
    );

}

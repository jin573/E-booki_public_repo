package com.be.ebooki.repository;

import com.be.ebooki.domain.BookSpine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSpineRepository extends JpaRepository<BookSpine, Integer> {

   }

package com.be.ebooki.repository;

import com.be.ebooki.domain.BookAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookAssetRepository extends JpaRepository<BookAsset, Integer> {

   }

package com.be.ebooki.repository;

import com.be.ebooki.domain.Emoticon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmoticonRepository extends JpaRepository<Emoticon, Integer> {
    List<Emoticon> findByCommentId(Integer commentId);
}

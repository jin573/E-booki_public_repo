package com.be.ebooki.repository;

import com.be.ebooki.domain.EmojiType;
import com.be.ebooki.domain.Emoticon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmoticonRepository extends JpaRepository<Emoticon, Integer> {
    List<Emoticon> findByCommentId(Integer commentId);
    List<Emoticon> findByCommentIdAndUserId(Integer commentId, Integer userId);
    Optional<Emoticon> findByCommentIdAndUserIdAndEmoji(
            Integer commentId,
            Integer userId,
            EmojiType emoji
    );

}

package com.be.ebooki.service;

import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.Like;
import com.be.ebooki.domain.User;
import com.be.ebooki.repository.BookRepository;
import com.be.ebooki.repository.LikeRepository;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public String toggleLike(Integer userId, Integer bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));

        return likeRepository.findByUserAndBook(user, book)
                .map(like -> { // 이미 찜되어 있으면 삭제
                    likeRepository.delete(like);
                    return "찜 해제 완료";
                })
                .orElseGet(() -> { // 없으면 추가
                    likeRepository.save(Like.builder().user(user).book(book).build());
                    return "찜 등록 완료";
                });
    }
}

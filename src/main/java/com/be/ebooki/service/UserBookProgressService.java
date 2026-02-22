package com.be.ebooki.service;
import com.be.ebooki.domain.Book;
import com.be.ebooki.domain.User;
import com.be.ebooki.domain.UserBookProgress;
import com.be.ebooki.repository.BookRepository;
import com.be.ebooki.repository.UserBookProgressRepository;
import com.be.ebooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserBookProgressService {

    private final UserBookProgressRepository userBookProgressRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;


    @Transactional
    public UserBookProgress createProgress(Integer userId, Integer bookId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("책 없음"));


        // 현재는 이미 존재하면 중복 생성을 방지해둠
        // 혹시 나중에 또 읽는거로 처리하기 위해서는 추후 수정해야함!
        boolean exists =
                userBookProgressRepository.existsByUserAndBook(user, book);
        if (exists) {
            throw new IllegalStateException("이미 진행중인 책입니다.");
        }

        UserBookProgress progress = UserBookProgress.builder()
                .user(user)
                .book(book)
                .percent(0.0)
                .cfi(null)
                .spineIndex(0)
                .updatedAt(LocalDateTime.now())
                .rating(null)
                .build();

        return userBookProgressRepository.save(progress);
    }

    @Transactional
    public void updateProgress(Integer userId,
                               Integer bookId,
                               Integer spineIndex,
                               String cfi,
                               Double percent) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책"));

        UserBookProgress progress =
                userBookProgressRepository.findByUserAndBook(user, book)
                        .orElseGet(() ->
                                userBookProgressRepository.save(
                                        UserBookProgress.builder()
                                                .user(user)
                                                .book(book)
                                                .spineIndex(spineIndex)
                                                .cfi(cfi)
                                                .percent(percent)
                                                .build()
                                )
                        );

        progress.updateProgress(spineIndex, cfi, percent);
    }


}


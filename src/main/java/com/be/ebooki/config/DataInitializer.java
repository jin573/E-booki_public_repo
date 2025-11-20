package com.be.ebooki.config;

import com.be.ebooki.domain.Book;
import com.be.ebooki.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void run(String... args) {

        if (isAlreadySeeded()) {
            log.info("📚 Seed skipped: Book 테이블에 기존 데이터가 있어 시드를 수행하지 않습니다.");
            return;
        }

        log.info("📚 Book Seed 데이터 생성 시작...");

        List<Book> books = List.of(
                newBook("해리포터와 마법사의 돌", "J.K. 롤링", "문학수첩", 15000, "https://example.com/hp1.jpg", 4.8),
                newBook("해리포터와 비밀의 방", "J.K. 롤링", "문학수첩", 16000, "https://example.com/hp2.jpg", 4.7),
                newBook("해리포터와 아즈카반의 죄수", "J.K. 롤링", "문학수첩", 17000, "https://example.com/hp3.jpg", 4.9),

                newBook("나미야 잡화점의 기적", "히가시노 게이고", "현대문학", 14000, "https://example.com/namiya.jpg", 4.6),
                newBook("어느 날, 내 죽음에 네가 들어왔다", "세이카 마이", "북로망스", 13500, "https://example.com/daydeath.jpg", 4.3),

                newBook("불편한 편의점", "김호연", "나무옆의자", 14000, "https://example.com/store1.jpg", 4.5),
                newBook("불편한 편의점 2", "김호연", "나무옆의자", 15000, "https://example.com/store2.jpg", 4.6),

                newBook("데미안", "헤르만 헤세", "민음사", 9000, "https://example.com/demian.jpg", 4.4),
                newBook("동물농장", "조지 오웰", "민음사", 8500, "https://example.com/farm.jpg", 4.8),

                newBook("참을 수 없는 존재의 가벼움", "밀란 쿤데라", "민음사", 12000, "https://example.com/lightness.jpg", 4.2),

                newBook("이기적 유전자", "리처드 도킨스", "을유문화사", 18000, "https://example.com/gene.jpg", 4.7),
                newBook("총, 균, 쇠", "재러드 다이아몬드", "문학사상", 19000, "https://example.com/guns.jpg", 4.5),

                newBook("아몬드", "손원평", "창비", 13000, "https://example.com/almond.jpg", 4.6),
                newBook("당신의 이름을 지어다가 며칠은 먹었다", "박준", "문학동네", 11000, "https://example.com/park.jpg", 4.4),

                newBook("세이노의 가르침", "세이노", "데이원", 6000, "https://example.com/seino.jpg", 4.1),
                newBook("역행자", "자청", "웅진지식하우스", 15000, "https://example.com/reverse.jpg", 4.2),

                newBook("종이여자", "기욤 뮈소", "밝은세상", 13500, "https://example.com/woman.jpg", 4.3),
                newBook("파리의 아파트", "기욤 뮈소", "밝은세상", 14500, "https://example.com/paris.jpg", 4.4),

                newBook("죽고 싶지만 떡볶이는 먹고 싶어", "백세희", "흔", 13000, "https://example.com/tteok.jpg", 4.0),
                newBook("무례한 사람에게 웃으며 대처하는 법", "정문정", "가나출판사", 14000, "https://example.com/rude.jpg", 4.1)
        );

        bookRepository.saveAll(books);

        log.info("📚 Book Seed 데이터 삽입 완료! 총 {}권", books.size());
    }

    private boolean isAlreadySeeded() {
        return bookRepository.count() > 0L;
    }

    private Book newBook(String title, String author, String publisher, int price, String image, double rating) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setPublisher(publisher);
        b.setPrice(price);
        b.setBookImage(image);
        b.setRating(rating);
        return b;
    }
}

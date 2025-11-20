package com.be.ebooki.service;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.repository.CommentRepository;
import com.be.ebooki.repository.EmoticonRepository;
import com.be.ebooki.repository.HighlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;
    private final EmoticonRepository emoticonRepository;

    public ReadingResponse.EnterDTO getInitialData(Integer bookId) {

        var highlights = highlightRepository.findByBookId(bookId);

        var highlightDTOs = highlights.stream()
                .map(h -> {

                    // 각 하이라이트의 댓글 조회
                    var comments = commentRepository.findByHighlightId(h.getId());

                    var commentDTOs = comments.stream()
                            .map(c -> {

                                // 각 댓글의 이모티콘 조회
                                var emoticons = emoticonRepository.findByCommentId(c.getId());

                                var emoticonDTOs = emoticons.stream()
                                        .map(e -> ReadingResponse.EmoticonDTO.builder()
                                                .id(e.getId())
                                                .userId(e.getUserId())
                                                .commentId(e.getCommentId())
                                                .emoji(e.getEmoji().name())
                                                .createdAt(e.getCreatedAt())
                                                .build())
                                        .toList();

                                return ReadingResponse.CommentDTO.builder()
                                        .id(c.getId())
                                        .userId(c.getUserId())
                                        .highlightId(c.getHighlightId())
                                        .text(c.getText())
                                        .createdAt(c.getCreatedAt())
                                        .emoticons(emoticonDTOs)
                                        .build();
                            })
                            .toList();

                    return ReadingResponse.HighlightDTO.builder()
                            .id(h.getId())
                            .userId(h.getUserId())
                            .teamId(h.getTeamId())
                            .spineIndex(h.getSpineIndex())
                            .cfi(h.getCfi())
                            .text(h.getText())
                            .color(h.getColor().name())
                            .createdAt(h.getCreatedAt())
                            .comments(commentDTOs)
                            .build();
                })
                .toList();

        return ReadingResponse.EnterDTO.builder()
                .bookId(bookId)
                .highlights(highlightDTOs)
                .build();
    }
}

package com.be.ebooki.service;
import com.be.ebooki.domain.Highlight;
import com.be.ebooki.dto.HighlightRequest;
import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.enums.HighlightColor;
import com.be.ebooki.repository.CommentRepository;
import com.be.ebooki.repository.EmoticonRepository;
import com.be.ebooki.repository.HighlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;
    private final EmoticonRepository emoticonRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ReadingResponse.HighlightListDTO getHighlights(Integer bookId) {

        var highlightDTOs = highlightRepository.findByBookId(bookId)
                .stream()
                .map(h -> ReadingResponse.HighlightDTO.builder()
                        .id(h.getId())
                        .userId(h.getUserId())
                        .teamId(h.getTeamId())
                        .bookId(h.getBookId())
                        .spineIndex(h.getSpineIndex())
                        .cfi(h.getCfi())
                        .text(h.getText())
                        .color(h.getColor().toString())
                        .build())
                .toList();

        return ReadingResponse.HighlightListDTO.builder()
                .bookId(bookId)
                .highlights(highlightDTOs)
                .build();
    }

    public List<ReadingResponse.CommentDTO> getComments(Integer highlightId) {

        return commentRepository.findByHighlightId(highlightId)
                .stream()
                .map(c -> ReadingResponse.CommentDTO.builder()
                        .id(c.getId())
                        .userId(c.getUserId())
                        .highlightId(c.getHighlightId())
                        .text(c.getText())
                        .createdAt(c.getCreatedAt())
                        .emoticons(buildEmoticonCount(c.getId()))
                        .build()
                )
                .toList();
    }

    private ReadingResponse.EmoticonCountDTO buildEmoticonCount(Integer commentId) {

        var emoticonList = emoticonRepository.findByCommentId(commentId);

        int likeCount = (int) emoticonList.stream()
                .filter(e -> e.getEmoji().name().equals("LIKE"))
                .count();

        int cryCount = (int) emoticonList.stream()
                .filter(e -> e.getEmoji().name().equals("CRY"))
                .count();

        return ReadingResponse.EmoticonCountDTO.builder()
                .likeCount(likeCount)
                .cryCount(cryCount)
                .build();
    }


    //하이라이트 생성
    public void createHighlight(HighlightRequest request) {
        Highlight highlight = Highlight.builder()
                .userId(1) //추후 getcurrendId
                .teamId(request.getTeamId())
                .bookId(request.getBookId())
                .spineIndex(request.getSpineIndex())
                .cfi(request.getCfi())
                .text(request.getText())
                .color(HighlightColor.valueOf(request.getColor()))
                .createdAt(System.currentTimeMillis())
                .build();

        Highlight saved = highlightRepository.save(highlight);

        //STOMP 얹기
        simpMessagingTemplate.convertAndSend("/topic/teams/" + saved.getTeamId() + "/books/" + saved.getBookId(),
                ReadingResponse.HighlightDTO.builder()
                        .id(saved.getId())
                        .userId(saved.getUserId())
                        .teamId(saved.getTeamId())
                        .bookId(saved.getBookId())
                        .spineIndex(saved.getSpineIndex())
                        .cfi(saved.getCfi())
                        .text(saved.getText())
                        .color(saved.getColor().name())
                        .build()
                );
    }
}

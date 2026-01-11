package com.be.ebooki.service;

import com.be.ebooki.domain.*;
import com.be.ebooki.dto.ReadingRequest;

import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.dto.StompResponse;
import com.be.ebooki.enums.EmojiType;
import com.be.ebooki.enums.HighlightColor;
import com.be.ebooki.enums.MessageType;
import com.be.ebooki.repository.BookRepository;
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
    private final TeamService teamService;


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

    public List<ReadingResponse.CommentDTO> getComments(Integer highlightId, Integer userId) {

        return commentRepository.findByHighlightId(highlightId)
                .stream()
                .map(c -> ReadingResponse.CommentDTO.builder()
                        .id(c.getId())
                        .userId(c.getUserId())
                        .highlightId(c.getHighlightId())
                        .text(c.getText())
                        .createdAt(c.getCreatedAt())
                        .emoticons(buildEmoticonCount(c.getId()))
                        .myEmoticon(buildUserEmoticon(c.getId(), userId))
                        .build()
                )
                .toList();
    }

    private ReadingResponse.EmoticonCountDTO buildEmoticonCount(Integer commentId) {

        var emoticonList = emoticonRepository.findByCommentId(commentId);

        int smileCount = (int) emoticonList.stream()
                .filter(e -> e.getEmoji().name().equals("SMILE"))
                .count();

        int likeCount = (int) emoticonList.stream()
                .filter(e -> e.getEmoji().name().equals("LIKE"))
                .count();

        return ReadingResponse.EmoticonCountDTO.builder()
                .smileCount(smileCount)
                .likeCount(likeCount)
                .build();
    }
    private ReadingResponse.UserEmoticonDTO buildUserEmoticon(Integer commentId, Integer userId) {

        var list = emoticonRepository.findByCommentIdAndUserId(commentId, userId);

        boolean smiled = list.stream().anyMatch(e -> e.getEmoji() == EmojiType.SMILE);
        boolean liked = list.stream().anyMatch(e -> e.getEmoji() == EmojiType.LIKE);

        return ReadingResponse.UserEmoticonDTO.builder()
                .smiled(smiled)
                .liked(liked)
                .build();
    }

    /** 하이라이트 생성 */
    public ReadingResponse.HighlightDTO createHighlight(
            ReadingRequest.CreateHighlightDTO req,
            Integer userId,
            Integer teamId) {

        //해당 유저가 정말로 해당 팀에 속해있는가?
        if(!teamService.validateMember(teamId, userId)){
            throw new IllegalArgumentException("존재하지 않는 팀원입니다.");
        }

        //팀의 책인지 검증
        if (!teamService.validateTeamBook(teamId, req.getBookId())) {
            throw new IllegalArgumentException("팀에 속하지 않은 책입니다.");
        }


        Highlight highlight = highlightRepository.save(
                Highlight.builder()
                    .userId(userId)
                    .teamId(teamId)
                    .bookId(req.getBookId())
                    .spineIndex(req.getSpineIndex())
                    .cfi(req.getCfi())
                    .text(req.getText())
                    .color(HighlightColor.valueOf(req.getColor()))
                    .build());


        ReadingResponse.HighlightDTO highlightDTO = ReadingResponse.HighlightDTO.builder()
                .id(highlight.getId())
                .userId(highlight.getUserId())
                .teamId(highlight.getTeamId())
                .bookId(highlight.getBookId())
                .spineIndex(highlight.getSpineIndex())
                .cfi(highlight.getCfi())
                .text(highlight.getText())
                .color(highlight.getColor().name())
                .build();

        //STOMP 얹기
        simpMessagingTemplate.convertAndSend("/topic/teams/" + highlight.getTeamId() + "/books/" + highlight.getBookId(),
                new StompResponse<>(MessageType.HIGHLIGHT_CREATED, highlight.getTeamId(), highlight.getBookId(), highlightDTO)
        );

        return highlightDTO;
    }

    /** 댓글 생성 */
    public ReadingResponse.CommentDTO createComment(
            ReadingRequest.CreateCommentDTO req,
            Integer userId,
            Integer teamId) {

        if(!teamService.validateMember(teamId, userId)){
            throw new IllegalArgumentException("존재하지 않는 팀원입니다.");
        }

        Highlight highlight = highlightRepository.findById(req.getHighlightId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 하이라이트 입니다."));

        if (!highlight.getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("해당 팀에서 접근할 수 없는 하이라이트입니다.");
        }

        //코멘트 저장
        Comment comment = Comment.builder()
                .userId(userId)
                .highlightId(highlight.getId())
                .text(req.getText())
                .createdAt(System.currentTimeMillis())
                .build();

        commentRepository.save(comment);


        //코멘트 dto 변환 후 stomp 전송
        ReadingResponse.CommentDTO commentDTO = ReadingResponse.CommentDTO.builder()
                .id(comment.getId())
                .userId(userId)
                .bookId(highlight.getBookId())
                .highlightId(comment.getHighlightId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .emoticons(new ReadingResponse.EmoticonCountDTO(0, 0))
                .myEmoticon(new ReadingResponse.UserEmoticonDTO(false, false))
                .build();

        simpMessagingTemplate.convertAndSend("/topic/teams/" + teamId + "/books/" + commentDTO.getBookId(),
                new StompResponse<>(MessageType.COMMENT_CREATED, teamId, commentDTO.getBookId(), commentDTO)
        );
        return commentDTO;
    }

    public ReadingResponse.EmoticonCountDTO toggleEmoticon(Integer commentId, Integer userId, EmojiType emojiType) {

        var existing = emoticonRepository
                .findByCommentIdAndUserIdAndEmoji(commentId, userId, emojiType);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        Highlight highlight = highlightRepository.findById(comment.getHighlightId())
                .orElseThrow(() -> new IllegalStateException("잘못된 하이라이트 입니다."));

        Integer teamId = highlight.getTeamId();
        Integer bookId = highlight.getBookId();

        if (!teamService.validateMember(teamId, userId)) {
            throw new IllegalArgumentException("팀 멤버만 이모지를 사용할 수 있습니다.");
        }

        if (existing.isPresent()) {
            emoticonRepository.delete(existing.get());
        } else {
            Emoticon newEmoji = new Emoticon();
            newEmoji.setUserId(userId);
            newEmoji.setCommentId(commentId);
            newEmoji.setEmoji(emojiType);
            emoticonRepository.save(newEmoji);

            simpMessagingTemplate.convertAndSend("/topic/teams/" + teamId + "/books/" + bookId,
                    new StompResponse<>(MessageType.EMOJI_CREATED, teamId, bookId, buildEmoticonCount(commentId))
            );
        }

        return buildEmoticonCount(commentId);
    }


}

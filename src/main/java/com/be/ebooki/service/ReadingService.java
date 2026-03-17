package com.be.ebooki.service;

import com.be.ebooki.domain.*;
import com.be.ebooki.dto.ReadingRequest;

import com.be.ebooki.dto.ReadingResponse;
import com.be.ebooki.dto.StompResponse;
import com.be.ebooki.enums.EmojiType;
import com.be.ebooki.enums.HighlightColor;

import com.be.ebooki.enums.MessageType;
import com.be.ebooki.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;
    private final EmoticonRepository emoticonRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final TeamService teamService;
    private final UserRepository userRepository;

    private final TeamUserRepository teamUserRepository;
    private final BookRepository bookRepository;
    private final UserBookProgressRepository userBookProgressRepository;


    public ReadingResponse.HighlightListDTO getHighlights(Integer bookId, Integer teamId) {

        var highlightDTOs = highlightRepository.findByBookIdAndTeamId(bookId, teamId)
                .stream()
                .map(h -> ReadingResponse.HighlightDTO.builder()
                        .id(h.getId())
                        .userId(h.getUserId())
                        .teamId(h.getTeamId())
                        .bookId(h.getBookId())
                        .spineIndex(h.getSpineIndex())
                        .cfi(h.getCfi())
                        .text(h.getText())
                        .color(h.getColor())
                        .createdAt(h.getCreatedAt())
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
        // TeamUser에서 색 조회
        TeamUser teamUser = teamUserRepository.findByTeam_IdAndUser_Id(teamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닙니다."));
        Highlight highlight = highlightRepository.save(
                Highlight.builder()
                        .userId(userId)
                        .teamId(teamId)
                        .bookId(req.getBookId())
                        .spineIndex(req.getSpineIndex())
                        .cfi(req.getCfi())
                        .text(req.getText())
                        .color(HighlightColor.valueOf(teamUser.getUserColor().name()))
                        .build());

        ReadingResponse.HighlightDTO highlightDTO = ReadingResponse.HighlightDTO.builder()
                .id(highlight.getId())
                .userId(highlight.getUserId())
                .teamId(highlight.getTeamId())
                .bookId(highlight.getBookId())
                .spineIndex(highlight.getSpineIndex())
                .cfi(highlight.getCfi())
                .text(highlight.getText())
                .color(highlight.getColor())
                .createdAt(highlight.getCreatedAt())
                .build();

        //STOMP 얹기
        simpMessagingTemplate.convertAndSend("/topic/teams/" + highlight.getTeamId() + "/books/" + highlight.getBookId(),
                new StompResponse<>(MessageType.HIGHLIGHT_CREATED, highlight.getTeamId(), highlight.getBookId(), highlightDTO)
        );

        return highlightDTO;
    }
    /** 하이라이트 삭제 */
    @Transactional
    public void deleteHighlight(Integer highlightId, Integer userId, Integer teamId) {

        Highlight highlight = highlightRepository.findById(highlightId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 하이라이트입니다."));

        if (!highlight.getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("팀이 일치하지 않습니다.");
        }
        if (!highlight.getUserId().equals(userId)) {
            throw new IllegalArgumentException("하이라이트 삭제 권한이 없습니다.");
        }

        

        Integer bookId = highlight.getBookId();

        //댓글 조회
        List<Comment> comments = commentRepository.findByHighlightId(highlightId);

        //이모티콘 삭제
        for (Comment comment : comments) {
            emoticonRepository.deleteAllByCommentId(comment.getId());
        }

        //하이라이트에 달린 댓글 삭제
        commentRepository.deleteAllByHighlightId(highlightId);
        //하이라이트 삭제
        highlightRepository.delete(highlight);

        simpMessagingTemplate.convertAndSend(
                "/topic/teams/" + teamId + "/books/" + bookId,
                new StompResponse<>(
                        MessageType.HIGHLIGHT_DELETED,
                        teamId,
                        bookId,
                        highlightId
                )
        );
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
    /** 댓글 수정 */
    @Transactional
    public ReadingResponse.CommentDTO updateComment(
            Integer commentId,
            ReadingRequest.UpdateCommentDTO req,
            Integer userId,
            Integer teamId
    ) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        Highlight highlight = highlightRepository.findById(comment.getHighlightId())
                .orElseThrow(() -> new IllegalStateException("잘못된 하이라이트입니다."));

        if (!highlight.getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("팀이 일치하지 않습니다.");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.setText(req.getText());

        Integer bookId = highlight.getBookId();

        ReadingResponse.CommentDTO commentDTO =
                ReadingResponse.CommentDTO.builder()
                        .id(comment.getId())
                        .userId(comment.getUserId())
                        .highlightId(comment.getHighlightId())
                        .bookId(bookId)
                        .text(comment.getText())
                        .createdAt(comment.getCreatedAt())
                        .emoticons(buildEmoticonCount(comment.getId()))
                        .myEmoticon(buildUserEmoticon(comment.getId(), userId))
                        .build();

        simpMessagingTemplate.convertAndSend(
                "/topic/teams/" + teamId + "/books/" + bookId,
                new StompResponse<>(
                        MessageType.COMMENT_UPDATED,
                        teamId,
                        bookId,
                        commentDTO
                )
        );

        return commentDTO;
    }


    /** 댓글 삭제 */
    @Transactional
    public void deleteComment(Integer commentId, Integer userId, Integer teamId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        Highlight highlight = highlightRepository.findById(comment.getHighlightId())
                .orElseThrow(() -> new IllegalStateException("잘못된 하이라이트입니다."));

        if (!highlight.getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("팀이 일치하지 않습니다.");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        Integer bookId = highlight.getBookId();

        emoticonRepository.deleteAllByCommentId(comment.getId());

        commentRepository.delete(comment);

        simpMessagingTemplate.convertAndSend(
                "/topic/teams/" + teamId + "/books/" + bookId,
                new StompResponse<>(
                        MessageType.COMMENT_DELETED,
                        teamId,
                        bookId,
                        commentId
                )
        );
    }


    public ReadingResponse.EmoticonCountDTO toggleEmoticon(Integer commentId, Integer teamId, Integer userId, EmojiType emojiType) {

        var existing = emoticonRepository
                .findByCommentIdAndUserIdAndEmoji(commentId, userId, emojiType);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        Highlight highlight = highlightRepository.findById(comment.getHighlightId())
                .orElseThrow(() -> new IllegalStateException("잘못된 하이라이트 입니다."));

        Integer bookId = highlight.getBookId();

        if (!teamService.validateMember(teamId, userId)) {
            throw new IllegalArgumentException("팀 멤버만 이모지를 사용할 수 있습니다.");
        }

        if (existing.isPresent()) {
            emoticonRepository.delete(existing.get());
            simpMessagingTemplate.convertAndSend(
                    "/topic/teams/" + teamId + "/books/" + bookId,
                    new StompResponse<>(
                            MessageType.EMOJI_DELETED,
                            teamId,
                            bookId,
                            buildEmoticonCount(commentId)
                    )
            );
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

    @Transactional(readOnly = true)
    public ReadingResponse.ReadingEntryDTO getReadingEntry(
            Integer userId,
            Integer teamId,
            Integer bookId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("책 없음"));

        UserBookProgress progress =
                userBookProgressRepository.findByUserIdAndBookId(userId, bookId)
                        .orElseThrow(() -> new IllegalStateException("독서 진행 정보 없음"));

        //  팀에 속한 전체 하이라이트 조회 (지금은 전부)
        List<Highlight> highlights =
                highlightRepository.findAllByBookIdAndTeamId(
                        bookId,
                        teamId
                );

        return ReadingResponse.ReadingEntryDTO.builder()
                .bookId(book.getId())
                .progress(
                        ReadingResponse.ProgressDTO.builder()
                                .cfi(progress.getCfi())
                                .spineIndex(progress.getSpineIndex())
                                .build()
                )
                .highlights(
                        highlights.stream()
                                .map(h -> ReadingResponse.HighlightDTO.builder()
                                        .id(h.getId())
                                        .userId(h.getUserId())
                                        .teamId(h.getTeamId())
                                        .bookId(h.getBookId())
                                        .spineIndex(h.getSpineIndex())
                                        .cfi(h.getCfi())
                                        .text(h.getText())
                                        .color(h.getColor())
                                        .build()
                                )
                                .toList()
                )
                .build();


    }


}

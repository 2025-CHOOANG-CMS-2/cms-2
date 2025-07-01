package kr.ac.dhuniv.notice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import kr.ac.dhuniv.notice.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse {

    @JsonProperty("BOARD_ID")
    private Long boardId;
    @JsonProperty("BOARD_TYPE")
    private String boardType;
    @JsonProperty("BOARD_NAME")
    private String boardName;
    @JsonProperty("CREATED_AT")
    private LocalDateTime createdAt;
    @JsonProperty("MODIFIED_AT")
    private LocalDateTime modifiedAt;
    @JsonProperty("CREATED_BY")
    private String createdBy;
    @JsonProperty("MODIFIED_BY")
    private String modifiedBy;

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .boardName(board.getBoardName())
                .createdAt(board.getCreatedAt())
                .modifiedAt(board.getModifiedAt())
                .createdBy(board.getCreatedBy())
                .modifiedBy(board.getModifiedBy())
                .build();
    }
}
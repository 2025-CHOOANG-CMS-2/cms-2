package kr.ac.dhuniv.notice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequest {

    @NotNull(message = "게시판 ID는 필수입니다.")
    @JsonProperty("BOARD_ID")
    private Long boardId; // ⭐ 이 부분이 반드시 Long 타입이어야 합니다! ⭐

    @JsonProperty("POST_NO")
    private Integer postNo;

    @NotBlank(message = "제목은 필수입니다.")
    @JsonProperty("TITLE")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @JsonProperty("CONTENT")
    private String content;

    @JsonProperty("START_DATE")
    private LocalDate startDate;

    @JsonProperty("END_DATE")
    private LocalDate endDate;

    @NotNull(message = "중요 공지 여부는 필수입니다.")
    @JsonProperty("IS_IMPORTANT")
    private Boolean isImportant;

    @NotNull(message = "게시 여부는 필수입니다.")
    @JsonProperty("IS_PUBLISHED")
    private Boolean isPublished;

    @JsonProperty("ATTACHMENT_UUIDS")
    private List<UUID> attachmentUuids; // 첨부파일 UUID 목록
}
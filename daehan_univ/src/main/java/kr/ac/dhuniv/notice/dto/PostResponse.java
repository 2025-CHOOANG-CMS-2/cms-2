package kr.ac.dhuniv.notice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import kr.ac.dhuniv.notice.domain.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // ⭐ Setter 추가 (PostController에서 필드를 설정하기 위함) ⭐

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter // ⭐ Setter 추가 ⭐
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {

    @JsonProperty("POST_ID")
    private Long postId;
    @JsonProperty("BOARD_ID")
    private Long boardId;
    @JsonProperty("BOARD_NAME")
    private String boardName;
    @JsonProperty("POST_NO")
    private Integer postNo;
    @JsonProperty("TITLE")
    private String title;
    @JsonProperty("CONTENT")
    private String content;
    @JsonProperty("VIEW_COUNT")
    private Integer viewCount;
    @JsonProperty("CREATED_AT")
    private LocalDateTime createdAt;
    @JsonProperty("MODIFIED_AT")
    private LocalDateTime modifiedAt;
    @JsonProperty("WRITER_EMPL_ID")
    private String writerEmplId;
    @JsonProperty("WRITER_NAME") // ⭐ 추가: 작성자 이름 ⭐
    private String writerName;
    @JsonProperty("WRITER_DEPT_NAME") // ⭐ 추가: 작성 부서 이름 ⭐
    private String writerDeptName;
    @JsonProperty("MODIFIED_BY")
    private String modifiedBy;
    @JsonProperty("START_DATE")
    private LocalDate startDate;
    @JsonProperty("END_DATE")
    private LocalDate endDate;
    @JsonProperty("IS_IMPORTANT")
    private Boolean isImportant;
    @JsonProperty("IS_PUBLISHED")
    private Boolean isPublished;
    @JsonProperty("ATTACHMENTS")
    private List<AttachmentResponse> attachments;

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static PostResponse from(Post post) {
        List<AttachmentResponse> attachmentResponses = post.getAttachments().stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());

        return PostResponse.builder()
                .postId(post.getPostId())
                .boardId(post.getBoard().getId())
                .boardName(post.getBoard().getBoardName())
                .postNo(post.getPostNo())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .writerEmplId(post.getWriterEmplId())
                // ⭐ 중요: writerName과 writerDeptName은 Post 엔티티에 직접 저장되지 않으므로,
                // 여기서 빌더로 설정하지 않습니다. PostController에서 조회하여 Setter로 설정할 것입니다. ⭐
                // .writerName(post.getWriterName()) // Post 엔티티에 해당 필드가 없다면 이렇게 직접 매핑하지 않습니다.
                // .writerDeptName(post.getDeptName()) // Post 엔티티에 해당 필드가 없다면 이렇게 직접 매핑하지 않습니다.
                .modifiedBy(post.getModifiedBy())
                .startDate(post.getStartDate())
                .endDate(post.getEndDate())
                .isImportant(post.getIsImportant())
                .isPublished(post.getIsPublished())
                .attachments(attachmentResponses)
                .build();
    }
}
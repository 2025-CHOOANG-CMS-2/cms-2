package kr.ac.dhuniv.notice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import kr.ac.dhuniv.notice.domain.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID; // java.util.UUID 임포트 확인

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    @JsonProperty("FILE_ID")
    private Long fileId;
    @JsonProperty("FILE_ORDER")
    private Integer fileOrder;
    @JsonProperty("UUID")
    private String uuid; // String 타입 유지
    @JsonProperty("ORIGINAL_FILE_NAME")
    private String originalFileName;
    @JsonProperty("FILE_SIZE")
    private Long fileSize;
    @JsonProperty("FILE_PATH") // ⭐ 추가: filePath 필드 ⭐
    private String filePath;
    @JsonProperty("MIME_TYPE") // ⭐ 추가: mimeType 필드 ⭐
    private String mimeType;
    @JsonProperty("POST_ID")
    private Long postId; // 어떤 게시글에 연결되었는지

    @JsonProperty("CREATED_AT")
    private LocalDateTime createdAt;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .fileId(attachment.getId())
                .fileOrder(attachment.getFileOrder())
                .uuid(attachment.getUuid()) // Attachment 엔티티의 getUuid()는 String을 반환
                .originalFileName(attachment.getOriginalFileName())
                .fileSize(attachment.getFileSize())
                .filePath(attachment.getFilePath()) // Attachment 엔티티의 getFilePath() 호출
                .mimeType(attachment.getMimeType()) // Attachment 엔티티의 getMimeType() 호출
                .postId(attachment.getPost() != null ? attachment.getPost().getPostId() : null) // Post 엔티티의 ID 필드명은 postId
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}

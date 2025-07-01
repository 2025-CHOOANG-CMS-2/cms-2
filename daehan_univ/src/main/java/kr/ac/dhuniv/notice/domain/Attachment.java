package kr.ac.dhuniv.notice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID; // java.util.UUID 임포트 확인

@Entity
@Table(name = "ATTACHMENT")
@Getter // 모든 필드에 대한 getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 protected로 자동 생성
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 (Builder와 함께 사용)
@Builder // ⭐ Builder 어노테이션 추가 ⭐
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화 (created_at 등)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id", nullable = false)
    private Long id; // ATTACHMENT 테이블의 PK

    @Column(name = "file_order")
    private Integer fileOrder; // 파일의 순번

    @Column(name = "uuid", nullable = false, unique = true, length = 255)
    private String uuid; // 고유한 파일 식별자 (UUID)

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName; // 원본 파일 이름

    @Column(name = "file_path", nullable = false, length = 500) // ⭐ filePath 필드 추가 ⭐
    private String filePath; // 파일 저장 경로

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // 파일 크기 (바이트 단위)

    @Column(name = "mime_type", nullable = false, length = 100) // ⭐ mimeType 필드 추가 ⭐
    private String mimeType; // 파일 MIME 타입

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id") // POST 테이블의 post_id 참조 (Nullable)
    private Post post;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 작성일시

    // --- 비즈니스 로직 메서드 (필요시) ---
    // ⭐ setPost 메서드 (이미 존재하지만, 다시 한번 포함) ⭐
    public void setPost(Post post) {
        this.post = post;
    }
    
    // Lombok @Getter가 이미 생성하지만, 명시적으로 포함하여 확인
    public String getUuid() { return this.uuid; }
    public String getFilePath() { return this.filePath; } // 명시적 getter
    public String getMimeType() { return this.mimeType; } // 명시적 getter
    public Long getId() { return this.id; } // PostResponse에서 사용될 getId()
}

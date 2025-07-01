package kr.ac.dhuniv.notice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy; // 이 어노테이션은 String 타입에 직접 매핑되지 않을 수 있습니다.
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "post") // ⭐ 테이블 이름이 "post"로 올바르게 지정되어 있습니다. ⭐
@Getter // 모든 필드에 대한 getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 protected로 자동 생성
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "post_no") // ⭐ post_no 컬럼 추가 ⭐
    private Integer postNo;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob // 대용량 텍스트 (CLOB) 매핑
    @Column(nullable = false)
    private String content;

    @Column(name = "writer_empl_id", nullable = false, length = 20) // ⭐ 타입 String, 길이 20으로 변경 ⭐
    private String writerEmplId; // 작성자 교직원 ID (varchar(20))

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성일시 (timestamp)

    @Column(name = "view_count", nullable = false)
    private Integer viewCount; // 조회수 (int4)

    @Column(name = "start_date")
    private LocalDate startDate; // 게시 시작일 (timestamp -> LocalDate로 매핑)

    @Column(name = "end_date")
    private LocalDate endDate; // 게시 종료일 (timestamp -> LocalDate로 매핑)

    @Column(name = "is_important", nullable = false)
    private Boolean isImportant; // 중요 공지 여부 (bool)

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished; // 게시 여부 (bool)

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt; // 최종 수정일시 (timestamp)

    // @LastModifiedBy // ⭐ String 타입에는 직접 매핑이 어려울 수 있으므로 주석 처리 또는 수동 관리 ⭐
    @Column(name = "modified_by", length = 50) // ⭐ 컬럼명 "modified_by", 타입 String, 길이 50으로 변경 ⭐
    private String modifiedBy; // 최종 수정자 (varchar(50))

    // 첨부파일 관계 (Post가 삭제되면 Attachment도 함께 삭제되도록 orphanRemoval = true 설정)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attachment> attachments = new HashSet<>();


    @Builder
    public Post(Board board, Integer postNo, String title, String content, String writerEmplId,
                Integer viewCount, LocalDate startDate, LocalDate endDate,
                Boolean isImportant, Boolean isPublished, String modifiedBy,
                LocalDateTime createdAt) { // ⭐ 여기에 createdAt 파라미터를 추가합니다. ⭐
        this.board = board;
        this.postNo = postNo;
        this.title = title;
        this.content = content;
        this.writerEmplId = writerEmplId;
        this.viewCount = (viewCount != null) ? viewCount : 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isImportant = isImportant;
        this.isPublished = isPublished;
        this.modifiedBy = modifiedBy;
        this.createdAt = createdAt; // ⭐ 생성자 내에서 필드에 값을 할당합니다. ⭐
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1 : this.viewCount + 1;
    }

    // 게시글 업데이트 메서드
    public void update(Board board, Integer postNo, String title, String content, String writerEmplId, // ⭐ 파라미터 변경 ⭐
                       LocalDate startDate, LocalDate endDate, Boolean isImportant,
                       Boolean isPublished, String modifiedBy) { // ⭐ 파라미터 변경 ⭐
        this.board = board;
        this.postNo = postNo; // postNo 업데이트
        this.title = title;
        this.content = content;
        this.writerEmplId = writerEmplId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isImportant = isImportant;
        this.isPublished = isPublished;
        this.modifiedBy = modifiedBy; // modifiedBy 업데이트
        // modifiedAt은 @LastModifiedDate에 의해 자동으로 업데이트됩니다.
    }

    // 게시 상태를 변경하는 Setter
    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    // ⭐ modifiedBy에 대한 setter 추가 (Auditing이 없을 경우 대비) ⭐
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // modifiedAt에 대한 setter (Auditing이 없을 경우 대비)
    public void setModifiedAt(LocalDateTime modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    // attachments 컬렉션을 설정하는 setter
    public void setAttachments(Set<Attachment> attachments) {
        this.attachments.clear();
        if (attachments != null) {
            attachments.forEach(attachment -> {
                this.attachments.add(attachment);
                attachment.setPost(this);
            });
        }
    }

    // 첨부파일 추가/제거 헬퍼 메서드 (양방향 관계 관리)
    public void addAttachment(Attachment attachment) {
        if (this.attachments == null) {
            this.attachments = new HashSet<>();
        }
        this.attachments.add(attachment);
        attachment.setPost(this);
    }

    public void removeAttachment(Attachment attachment) {
        if (this.attachments != null) {
            this.attachments.remove(attachment);
            attachment.setPost(null);
        }
    }
    // Lombok @Getter가 이미 생성하지만, 명시적으로 포함하여 확인 (오류 방지용)
    public Long getPostId() { return this.postId; }
    public Board getBoard() { return this.board; }
    public Integer getPostNo() { return this.postNo; } // postNo getter
    public String getTitle() { return this.title; }
    public String getContent() { return this.content; }
    public String getWriterEmplId() { return this.writerEmplId; } // writerEmplId getter
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public Integer getViewCount() { return this.viewCount; }
    public LocalDate getStartDate() { return this.startDate; }
    public LocalDate getEndDate() { return this.endDate; }
    public Boolean getIsImportant() { return this.isImportant; }
    public Boolean getIsPublished() { return this.isPublished; }
    public LocalDateTime getModifiedAt() { return this.modifiedAt; }
    public String getModifiedBy() { return this.modifiedBy; } // modifiedBy getter
    public Set<Attachment> getAttachments() { return this.attachments; }
}

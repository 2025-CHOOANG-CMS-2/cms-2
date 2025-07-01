package kr.ac.dhuniv.notice.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "BOARD") // 실제 DB 테이블 이름과 일치하는지 확인
@Getter // 모든 필드에 대한 getter 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 활성화
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false)
    private Long id; // BOARD 테이블의 PK (필드명 'id' 유지)

    @Column(name = "board_type", length = 50)
    private String boardType; // 게시판 종류 (예: '공지사항', '자유게시판')

    @Column(name = "board_name", nullable = false, length = 100)
    private String boardName; // 게시판 이름

    // 게시판에 속한 게시글들 (양방향 관계)
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Post> posts = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 일시

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt; // 수정 일시

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    // --- 비즈니스 로직 메서드 ---
    public void updateBoardName(String boardName, String modifiedBy) {
        this.boardName = boardName;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = LocalDateTime.now();
    }

    public void updateBoardType(String boardType, String modifiedBy) {
        this.boardType = boardType;
        this.modifiedBy = modifiedBy;
        this.modifiedAt = LocalDateTime.now();
    }
    
    // Lombok @Getter가 이미 생성하지만, 명시적으로 포함하여 확인 (오류 방지용)
    public Long getId() { return this.id; }
    public String getBoardType() { return this.boardType; }
    public String getBoardName() { return this.boardName; }
    public Set<Post> getPosts() { return this.posts; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public LocalDateTime getModifiedAt() { return this.modifiedAt; }
    public String getCreatedBy() { return this.createdBy; }
    public String getModifiedBy() { return this.modifiedBy; }
}

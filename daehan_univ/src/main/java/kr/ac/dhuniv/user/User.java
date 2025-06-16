package kr.ac.dhuniv.user;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_info")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL, AUTO_INCREMENT
    private Long id;  // 의미 없는 내부 PK

    @Column(name = "user_id", length = 20, nullable = false, unique = true)
    private String userId;  // 학번 or 사번

    @Column(name = "user_pw", length = 500, nullable = false)
    private String userPw;

    // ✅ 기존 userType 제거 (ManyToMany 구조로 대체)
    // @Column(name = "user_type", length = 20, nullable = false)
    // private String userType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_cnt", nullable = false)
    private Integer failedLoginCnt;

    // ✅ 다중 권한 관계 추가
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles", // 연결 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // 현재 엔티티(User)의 FK
            inverseJoinColumns = @JoinColumn(name = "role_id") // Role 엔티티 FK
    )
    private List<Role> roles;
}
package kr.ac.dhuniv.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_info")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL, AUTO_INCREMENT
    private Long id;  // 의미 없는 내부 PK

    @Column(name = "user_id", length = 20, nullable = false, unique = true)
    private String userId;  // 학번 or 사번

    @Column(name = "user_pw", length = 500, nullable = false)
    private String userPw;

    @Column(name = "user_type", columnDefinition = "CHAR(1)", nullable = false)
    private String userType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_cnt", nullable = false)
    private Integer failedLoginCnt;
}

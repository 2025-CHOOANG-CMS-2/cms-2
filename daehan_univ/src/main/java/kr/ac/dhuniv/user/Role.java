package kr.ac.dhuniv.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_info")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL, AUTO_INCREMENT
    private Long id;

    @Column(name = "role_name", length = 50, nullable = false, unique = true)
    private String roleName;  // 예: ROLE_ADMIN, ROLE_STUDENT
}
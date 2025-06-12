package kr.ac.dhuniv.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "untitled")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "user_id", length = 20, nullable = false, unique = true)
    private String userId; // 비즈니스 키

    @Column(name = "user_pw", length = 500)
    private String userPw;

    @Column(name = "field2", length = 255)
    private String field2;

    @Column(name = "field3", length = 255)
    private String field3;

    @Column(name = "field4", length = 255)
    private String field4;

    @Column(name = "field5", length = 255)
    private String field5;

    @Column(name = "field6", length = 255)
    private String field6;
}

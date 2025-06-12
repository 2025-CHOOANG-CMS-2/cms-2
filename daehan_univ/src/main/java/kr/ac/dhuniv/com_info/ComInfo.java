package kr.ac.dhuniv.com_info;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "com_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "com_no", length = 20, nullable = false, unique = true)
    private String comNo; // 원래의 비즈니스 키

    @Column(name = "com_type", length = 100)
    private String comType;

    @Column(name = "com_nm", length = 100)
    private String comNm;

    @Column(name = "com_cn", columnDefinition = "TEXT")
    private String comCn;
}
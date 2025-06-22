package kr.ac.dhuniv.counsel.domain;

import jakarta.persistence.*;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cnsl_aply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CnslAply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "cnsl_aply_id", length = 20, nullable = false, unique = true)
    private String cnslAplyId; // 비즈니스 키

    @Column(name = "aply_dttm")
    private LocalDateTime applyDateTime;

    @Column(name = "req_dttm")
    private LocalDateTime requestDateTime;

    @Column(name = "type_cd", length = 10)
    private String typeCode;

    @Column(name = "stat_cd", length = 10)
    private String statusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdInfo student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
    private EmplInfo employee;

    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 상담 신청 내용
    

}
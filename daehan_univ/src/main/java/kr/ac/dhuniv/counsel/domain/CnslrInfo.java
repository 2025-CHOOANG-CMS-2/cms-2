package kr.ac.dhuniv.counsel.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.*;

@Entity
@Table(name = "cnslr_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CnslrInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cnslrId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
    private EmplInfo employee;

    @Column(name = "cnsl_spec")
    private String cnslSpec;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "intro")
    private String intro;

    @Builder
    public CnslrInfo(EmplInfo employee, String cnslSpec, Boolean isActive, String intro) {
        this.employee = employee;
        this.cnslSpec = cnslSpec;
        this.isActive = isActive;
        this.intro = intro;
    }

    public void update(String cnslSpec, String intro, Boolean isActive) {
        this.cnslSpec = cnslSpec;
        this.intro = intro;
        this.isActive = isActive;
    }

    public void changeStatus(boolean isActive) {
        this.isActive = isActive;
    }
}
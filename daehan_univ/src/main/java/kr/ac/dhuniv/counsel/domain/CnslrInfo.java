package kr.ac.dhuniv.counsel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cnslr_info")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CnslrInfo {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cnslrId;

    // [수정] String 타입이 아닌, EmplInfo 객체를 직접 참조합니다.
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
	
	// 수정 로직을 위한 메소드 (Setter 대신 이 방법을 쓰는 것이 객체지향적)
    public void update(String cnslSpec, String intro, Boolean isActive) {
        this.cnslSpec = cnslSpec;
        this.intro = intro;
        this.isActive = isActive;
    }
    
    public void changeStatus(boolean isActive) {
        this.isActive = isActive;
    }
}

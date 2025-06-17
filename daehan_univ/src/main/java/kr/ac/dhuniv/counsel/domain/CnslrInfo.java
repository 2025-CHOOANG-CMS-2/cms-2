package kr.ac.dhuniv.counsel.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cnslr_info")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CnslrInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cnslrId;
	
	private String emplNo;
	private String cnslSpec;
	private boolean isActive;
	private String intro;
	
	public CnslrInfo(String emplNo, String cnslSpec, Boolean isActive, String intro) {
        this.emplNo = emplNo;
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

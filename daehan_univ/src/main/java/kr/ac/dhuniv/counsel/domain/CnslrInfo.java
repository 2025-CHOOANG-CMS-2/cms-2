package kr.ac.dhuniv.counsel.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cnslr_info")
public class CnslrInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long cnslrId;
	
	private String emplNo;
	private String cnslSpec;
	private boolean isActive;
	private String intro;
}

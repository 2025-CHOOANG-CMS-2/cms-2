package kr.ac.dhuniv.counsel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounselorListDto {
	private String counselorId;
	private String name;
	private String email;
	private String phone;
	private String specialty;
	private String status;
	private Long consultationCount;
	private Double averageRating;
}

package kr.ac.dhuniv.mileage.dto.student;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetencyGroupedMileageDTO {
	private String coreCompetencyName;
    private List<MileageProgramDTO> programs;
}

package kr.ac.dhuniv.core_cpt.dto.diagnosis;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetencyScoreDTO {
    private String competencyName;
    private Integer score;
}
package kr.ac.dhuniv.core_cpt.dto.emp_result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProgramRecommendationDTO {
    private Long prgId;
    private String prgNm;
    private String prgDesc;
    private String categoryName;
    private LocalDateTime aplyEndDate;
    private String imageUrl;
}

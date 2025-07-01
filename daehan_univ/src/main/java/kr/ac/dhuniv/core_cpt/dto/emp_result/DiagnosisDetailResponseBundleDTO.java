package kr.ac.dhuniv.core_cpt.dto.emp_result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DiagnosisDetailResponseBundleDTO {
    private String stdNo;
    private String stdName;
    private String departmentName;
    private Integer grade;

    private List<DiagnosisDetailWithColorDTO> diagnosisDetails;
}
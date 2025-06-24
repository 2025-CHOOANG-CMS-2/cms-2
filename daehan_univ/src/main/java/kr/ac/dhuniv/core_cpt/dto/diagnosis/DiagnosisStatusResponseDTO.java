package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * ✅ 학생 진단 현황 전체 응답 DTO
 * - 종합 점수, 수준, 최신 진단일, 상세 점수 목록 포함
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisStatusResponseDTO {
    private Integer totalScore;                  // 종합 점수 (모든 상위역량 진단 완료 시에만 값 존재)
    private String levelText;                    // 종합 수준 (예: 우수, 보통)
    private LocalDate latestDate;                // 최신 진단일
    private List<DiagnosisDetailDTO> details;    // 역량별 상세 점수 목록
}

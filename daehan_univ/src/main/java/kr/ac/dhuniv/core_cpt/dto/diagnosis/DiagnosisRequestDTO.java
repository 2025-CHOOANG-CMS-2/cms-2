package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.Data;
import java.util.Map;

/**
 * ✅ 진단 제출 요청 DTO
 * - 프론트에서 제출되는 데이터 구조
 */
@Data
public class DiagnosisRequestDTO {
    private String studentUserId;              // 학생 학번 또는 사용자 ID
    private Map<Long, Long> answers;           // 문항 ID -> 선택한 옵션 ID
    private int elapsedSeconds;                // 소요 시간 (초)
}
//package kr.ac.dhuniv.core_cpt.dto.result;
//
//import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisDetailDTO;
//import lombok.Builder;
//import lombok.Data;
//
//import java.util.List;
//
///**
// * ✅ DiagnosisResultResponseDTO
// * - 결과 분석 API의 최종 응답 DTO
// * - 학생의 최신 진단 결과 종합 점수, 수준, 상세 역량 점수를 담음
// */
//@Data
//@Builder
//public class DiagnosisResultResponseDTO {
//
//    /**
//     * 전체 종합 점수 (100점 만점 환산)
//     */
//    private int totalScore;
//
//    /**
//     * 점수에 따른 수준 텍스트 (예: 매우 우수, 우수, 보통, 개선 필요)
//     */
//    private String levelText;
//
//    /**
//     * 최신 진단 일자 (yyyy-MM-dd 형식 문자열)
//     */
//    private String latestDate;
//
//    /**
//     * 상위 역량별 점수 상세 목록
//     */
//    private List<DiagnosisDetailDTO> details;
//}

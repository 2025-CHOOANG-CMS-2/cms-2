//package kr.ac.dhuniv.core_cpt.service;
//
//import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
//import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
//
//import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisResultDTO;
//import kr.ac.dhuniv.core_cpt.repository.CoreCptEvalRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * ✅ DiagnosisResultService
// * - 점수 집계 + 결과 분석 처리
// */
//@Service
//@RequiredArgsConstructor
//public class DiagnosisResultService {
//
//    private final CoreCptEvalRepository coreCptEvalRepository;
//
//    /**
//     * ✅ getDiagnosisResult
//     * - 학생 진단 점수 집계
//     *
//     * @param stdNo 학생 번호
//     * @return DiagnosisResultDTO
//     */
//    @Transactional(readOnly = true)
//    public DiagnosisResultDTO getDiagnosisResult(Long stdNo) {
//        // ✅ 학생 응답 데이터 조회
//        List<CoreCptEval> evals = coreCptEvalRepository.findByStudentStdId(stdNo);
//
//        if (evals.isEmpty()) {
//            throw new NoSuchElementException("학생의 진단 데이터가 없습니다: " + stdNo);
//        }
//
//        // ✅ 학번(userId) 추출
//        String userId = evals.get(0).getStudent().getUser().getUserId();
//
//        // ✅ 최신 제출일시
//        LocalDateTime submittedAt = evals.stream()
//                .map(CoreCptEval::getAnswerDate)
//                .max(LocalDateTime::compareTo)
//                .orElse(LocalDateTime.now());
//
//        // ✅ 핵심역량별 점수 집계
//        Map<CoreCptInfo, List<CoreCptEval>> grouped = evals.stream()
//                .collect(Collectors.groupingBy(e -> e.getQuestion().getCoreCptInfo()));
//
//        List<DiagnosisResultDTO.CompetencyScore> competencyScores = new ArrayList<>();
//        int totalScoreSum = 0;
//        int competencyCount = 0;
//
//        for (Map.Entry<CoreCptInfo, List<CoreCptEval>> entry : grouped.entrySet()) {
//            CoreCptInfo cci = entry.getKey();
//            List<CoreCptEval> cciEvals = entry.getValue();
//
//            int cciScore = (int) cciEvals.stream()
//                    .mapToInt(e -> e.getSelectedOption().getScore())
//                    .average()
//                    .orElse(0);
//
//            totalScoreSum += cciScore;
//            competencyCount++;
//
//            competencyScores.add(DiagnosisResultDTO.CompetencyScore.builder()
//                    .cciId(cci.getCciId())
//                    .cciNm(cci.getCciNm())
//                    .score(cciScore)
//                    .build());
//        }
//
//        int totalScore = competencyCount > 0 ? totalScoreSum / competencyCount : 0;
//
//        // ✅ 최종 DTO 반환
//        return DiagnosisResultDTO.builder()
//                .stdNo(stdNo)
//                .userId(userId)
//                .submittedAt(submittedAt)
//                .totalScore(totalScore)
//                .competencies(competencyScores)
//                .build();
//    }
//}

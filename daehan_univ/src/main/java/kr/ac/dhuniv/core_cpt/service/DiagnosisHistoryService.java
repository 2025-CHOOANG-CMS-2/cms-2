//package kr.ac.dhuniv.core_cpt.service;
//
//import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
//import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisHistoryDTO;
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
// * ✅ DiagnosisHistoryService
// * - 진단 이력 조회 처리
// */
//@Service
//@RequiredArgsConstructor
//public class DiagnosisHistoryService {
//
//    private final CoreCptEvalRepository coreCptEvalRepository;
//
//    /**
//     * ✅ getDiagnosisHistory
//     * - 학생 진단 이력 데이터 반환
//     *
//     * @param stdNo 학생 번호
//     * @return DiagnosisHistoryDTO
//     */
//    @Transactional(readOnly = true)
//    public DiagnosisHistoryDTO getDiagnosisHistory(Long stdNo) {
//        List<CoreCptEval> evals = coreCptEvalRepository.findByStudentStdId(stdNo);
//
//        if (evals.isEmpty()) {
//            throw new NoSuchElementException("진단 이력이 없습니다: " + stdNo);
//        }
//
//        String userId = evals.get(0).getStudent().getUser().getUserId();
//
//        // ✅ evalCode 단위로 groupBy
//        Map<String, List<CoreCptEval>> grouped = evals.stream()
//                .collect(Collectors.groupingBy(CoreCptEval::getEvalCode));
//
//        // ✅ 이력 리스트 생성
//        List<DiagnosisHistoryDTO.HistoryItem> historyList = grouped.entrySet().stream()
//                .map(entry -> {
//                    String evalCode = entry.getKey();
//                    List<CoreCptEval> items = entry.getValue();
//
//                    Integer score = (int) items.stream()
//                            .mapToInt(e -> e.getSelectedOption().getScore())
//                            .average()
//                            .orElse(0);
//
//                    LocalDateTime submittedAt = items.stream()
//                            .map(CoreCptEval::getAnswerDate)
//                            .max(LocalDateTime::compareTo)
//                            .orElse(null);
//
//                    return DiagnosisHistoryDTO.HistoryItem.builder()
//                            .evalCode(evalCode)
//                            .submittedAt(submittedAt)
//                            .totalScore(score)
//                            .build();
//                })
//                .sorted(Comparator.comparing(DiagnosisHistoryDTO.HistoryItem::getSubmittedAt).reversed())
//                .collect(Collectors.toList());
//
//        return DiagnosisHistoryDTO.builder()
//                .stdNo(stdNo)
//                .userId(userId)
//                .history(historyList)
//                .build();
//    }
//}
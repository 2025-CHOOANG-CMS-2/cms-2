//package kr.ac.dhuniv.core_cpt.service;
//
//import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
//import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
//
//import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
//import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisRecommendationDTO;
//import kr.ac.dhuniv.core_cpt.repository.CoreCptEvalRepository;
//
//import kr.ac.dhuniv.core_cpt.repository.CoreCptCommentTemplateRepository;
//import kr.ac.dhuniv.core_cpt.repository.CoreCpt_NcsPrgInfoRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * ✅ DiagnosisRecommendationService
// * - 개선 방안 + 추천 프로그램 API 처리
// */
//@Service
//@RequiredArgsConstructor
//public class DiagnosisRecommendationService {
//
//    private final CoreCptEvalRepository coreCptEvalRepository;
//    private final CoreCptCommentTemplateRepository commentRepository;
//    private final CoreCpt_NcsPrgInfoRepository prgRepository;
//
//    /**
//     * ✅ getRecommendations
//     * - 개선 방안 + 프로그램 추천
//     *
//     * @param stdNo 학생 번호
//     * @return DiagnosisRecommendationDTO
//     */
//    @Transactional(readOnly = true)
//    public DiagnosisRecommendationDTO getRecommendations(Long stdNo) {
//        List<CoreCptEval> evals = coreCptEvalRepository.findByStudentStdId(stdNo);
//
//        if (evals.isEmpty()) {
//            throw new NoSuchElementException("진단 데이터 없음: " + stdNo);
//        }
//
//        String userId = evals.get(0).getStudent().getUser().getUserId();
//
//        // 제출일시
//        var submittedAt = evals.stream()
//                .map(CoreCptEval::getAnswerDate)
//                .max(LocalDateTime::compareTo)
//                .orElse(null);
//
//        // 핵심역량별 점수 집계
//        Map<CoreCptInfo, List<CoreCptEval>> grouped = evals.stream()
//                .collect(Collectors.groupingBy(e -> e.getQuestion().getCoreCptInfo()));
//
//        List<DiagnosisRecommendationDTO.RecommendationItem> items = new ArrayList<>();
//
//        for (var entry : grouped.entrySet()) {
//            CoreCptInfo cci = entry.getKey();
//            List<CoreCptEval> cciEvals = entry.getValue();
//
//            int score = (int) cciEvals.stream()
//                    .mapToInt(e -> e.getSelectedOption().getScore())
//                    .average()
//                    .orElse(0);
//
//            // 코멘트 조회
//            String comment = commentRepository
//                    .findTopByCoreCptAndMinScoreLessThanEqualOrderByMinScoreDesc(cci, score)
//                    .map(CoreCptCommentTemplate::getContent)
//                    .orElse("개선 코멘트를 준비 중입니다.");
//
//            // 프로그램 추천 조회
//            List<DiagnosisRecommendationDTO.ProgramItem> programs = prgRepository
//                    .findByCoreCpt(cci).stream()
//                    .map(p -> DiagnosisRecommendationDTO.ProgramItem.builder()
//                            .prgId(p.getPrgId())
//                            .prgNm(p.getPrgNm())
//                            .desc(p.getPrgDesc())
//                            .applyDeadline(p.getAplyEndDate().toString())
//                            .build())
//                    .collect(Collectors.toList());
//
//            items.add(DiagnosisRecommendationDTO.RecommendationItem.builder()
//                    .cciId(cci.getCciId())
//                    .cciNm(cci.getCciNm())
//                    .score(score)
//                    .comment(comment)
//                    .programs(programs)
//                    .build());
//        }
//
//        return DiagnosisRecommendationDTO.builder()
//                .stdNo(stdNo)
//                .userId(userId)
//                .submittedAt(submittedAt)
//                .recommendations(items)
//                .build();
//    }
//}
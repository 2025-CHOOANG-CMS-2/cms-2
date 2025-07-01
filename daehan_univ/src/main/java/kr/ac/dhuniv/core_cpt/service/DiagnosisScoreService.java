/*
package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisResponseDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

*/
/**
 * 점수 계산 로직
 *//*

@Service
@RequiredArgsConstructor
public class DiagnosisScoreService {

    private final CoreCptInfoRepository infoRepo;
    private final CoreCptQstRepository qstRepo;

//    @Transactional(readOnly = true)
//    public DiagnosisResponseDTO calculateScores(String evalCode, Map<Long, Long> answers) {
//        List<CoreCptInfo> roots = infoRepo.findByParentIsNullOrderByWeightAsc();
//
//        double totalWeighted = 0;
//        List<DiagnosisResponseDTO.CompetencyScoreDTO> details = new ArrayList<>();
//
//        for (CoreCptInfo root : roots) {
//            List<CoreCptQst> questions = collectQuestionsRecursively(root);
//
//            int sum = 0;
//            int max = 0;
//
//            for (CoreCptQst q : questions) {
//                // 제출 답안 점수
//                Long ansScore = answers.get(q.getQstId());
//                if (ansScore != null) sum += ansScore;
//
//                // 최대 점수: 문항의 옵션 템플릿
//                if (q.getOptionTemplate() != null) {
//                    max += q.getOptionTemplate().getScore();
//                }
//            }
//
//            double percentage = (max > 0) ? (sum * 100.0 / max) : 0;
//
//            details.add(
//                    DiagnosisResponseDTO.CompetencyScoreDTO.builder()
//                            .cciId(root.getCciId())
//                            .cciNm(root.getCciNm())
//                            .questionCount(questions.size())
//                            .sumScore(sum)
//                            .maxScore(max)
//                            .percentage(Math.round(percentage * 100) / 100.0)
//                            .weight(root.getWeight())
//                            .build()
//            );
//
//            totalWeighted += percentage * (root.getWeight() / 100.0);
//        }
//
//        return DiagnosisResponseDTO.builder()
//                .evalCode(evalCode)
//                .totalScore(Math.round(totalWeighted * 100) / 100.0)
//                .details(details)
//                .build();
//    }

  */
/*  *//*
*/
/**
     * 재귀적으로 하위 문항까지 수집
     *//*
*/
/*
    private List<CoreCptQst> collectQuestionsRecursively(CoreCptInfo node) {
        List<CoreCptQst> result = new ArrayList<>(qstRepo.findByCoreCptInfo(node));
        for (CoreCptInfo child : node.getChildren()) {
            result.addAll(collectQuestionsRecursively(child));
        }
        return result;
    }*//*

}*/

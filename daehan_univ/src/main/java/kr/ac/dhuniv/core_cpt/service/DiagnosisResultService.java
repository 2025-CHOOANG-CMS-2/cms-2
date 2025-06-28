package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;

import kr.ac.dhuniv.core_cpt.dto.result.CompetencyResultDTO;
import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisResultDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptCommentTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptEvalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ DiagnosisResultService
 * - 점수 집계 + 결과 분석 처리
 */
@Service
@RequiredArgsConstructor
public class DiagnosisResultService {

    private final CoreCptEvalRepository evalRepository;
    private final CoreCptCommentTemplateRepository commentTemplateRepository;
    /**
     * ✅ 최신 진단의 상위역량별 점수 + 코멘트를 조회
     * @param studentNo 학생 학번
     * @return List<CompetencyResultDTO>
     */
    public List<CompetencyResultDTO> getDiagnosisResults(String studentNo) {
        // DB에서 상위역량별 점수 데이터 조회
        List<Object[]> rawResults = evalRepository.findLatestCompetencyScores(studentNo);

        // 각 데이터 가공
        return rawResults.stream().map(row -> {
            // eval_id는 필요 없다면 생략 가능
            Long evalId = ((Number) row[0]).longValue();
            Long upperCciId = ((Number) row[1]).longValue();
            String competencyName = (String) row[2];
            String colorHex = (String) row[3];
            Integer score = ((Number) row[4]).intValue();

            // 점수에 맞는 코멘트 조회
            String comment = commentTemplateRepository.findCommentByScore(upperCciId, score);

            // DTO 생성
            return CompetencyResultDTO.builder()
                    .studentNo(studentNo)
                    .competencyName(competencyName)
                    .colorHex(colorHex)
                    .score(score)
                    .comment(comment)
                    .build();
        }).collect(Collectors.toList());
    }
}

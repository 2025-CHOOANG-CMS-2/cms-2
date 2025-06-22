package kr.ac.dhuniv.core_cpt.service;

import jakarta.transaction.Transactional;
import kr.ac.dhuniv.core_cpt.domain.*;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisDetailDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisStatusResponseDTO;
import kr.ac.dhuniv.core_cpt.repository.*;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisEvalService {

    private final CoreCptEvalRepository evalRepository;
    private final CoreCptEvalAnswerRepository answerRepo;
    private final CoreCpt_StdInfoRepository stdRepo;
    private final CoreCptQstRepository qstRepo;
    private final CoreCptOptionTemplateRepository optRepo;
    private final CoreCptInfoRepository infoRepository;

    /**
     * ✅ 진단 데이터 저장 및 점수 계산 결과 반환
     */
    public DiagnosisResponseDTO saveDiagnosis(Long cciId, DiagnosisRequestDTO dto) {
        StdInfo student = stdRepo.findByUserUserId(dto.getStudentUserId())
                .orElseThrow(() -> new IllegalArgumentException("생학 정보를 찾을 수 없습니다."));

        CoreCptEval eval = CoreCptEval.builder()
                .answerDate(LocalDateTime.now())
                .evalCode(UUID.randomUUID().toString().substring(0, 8))
                .student(student)
                .build();
        evalRepository.save(eval);

        dto.getAnswers().forEach((qstId, optId) -> {
            CoreCptOptionTemplate option = optRepo.findById(optId)
                    .orElseThrow(() -> new IllegalArgumentException("선택지 정보를 찾을 수 없습니다."));
            CoreCptEvalAnswer answer = CoreCptEvalAnswer.builder()
                    .eval(eval)
                    .question(qstRepo.findById(qstId).orElseThrow())
                    .selectedOption(option)
                    .answerScore(option.getScore())
                    .build();
            answerRepo.save(answer);
        });

        return buildDiagnosisResult(eval.getEvalId(), student.getStdId());
    }

    /**
     * ✅ 진단 현황 조회 시: 진단 내업이 없어도 기본 값 반환
     */
    public DiagnosisStatusResponseDTO getLatestDiagnosis(Long studentId) {
        Optional<Long> optionalEvalId = evalRepository.findLatestEvalIdByStudent(studentId);

        // 진단 내업이 없으면 모든 역량을 0점으로 구성
        if (optionalEvalId.isEmpty()) {
            List<CoreCptInfo> upperList = infoRepository.findRootCompetencies();
            List<DiagnosisDetailDTO> details = upperList.stream()
                    .map(upper -> DiagnosisDetailDTO.builder()
                            .competencyName(upper.getCciNm())
                            .score(0)
                            .avgScore(0)
                            .build())
                    .collect(Collectors.toList());

            DiagnosisStatusResponseDTO empty = new DiagnosisStatusResponseDTO();
            empty.setTotalScore(null);
            empty.setLevelText(null);
            empty.setLatestDate(null);
            empty.setDetails(details);
            return empty;
        }

        Long evalId = optionalEvalId.get();
        return buildDiagnosisStatusResult(evalId, studentId);
    }

    /**
     * ✅ 진단 제출 후 결과 DTO 생성
     */
    private DiagnosisResponseDTO buildDiagnosisResult(Long evalId, Long studentId) {
        var result = calculateScores(evalId, studentId);
        return DiagnosisResponseDTO.builder()
                .evalCode(result.evalCode)
                .totalScore(result.totalScore)
                .levelText(result.levelText)
                .details(result.details)
                .build();
    }

    /**
     * ✅ 진단 현황 결과 DTO 생성
     */
    private DiagnosisStatusResponseDTO buildDiagnosisStatusResult(Long evalId, Long studentId) {
        var result = calculateScores(evalId, studentId);
        DiagnosisStatusResponseDTO dto = new DiagnosisStatusResponseDTO();
        dto.setTotalScore(result.totalScore);
        dto.setLevelText(result.levelText);
        dto.setLatestDate(evalRepository.findLatestDate(studentId));
        dto.setDetails(result.details);
        return dto;
    }

    /**
     * ✅ 점수 공통 계산 (하위 → 상위역량 조립)
     */
    private ScoreResult calculateScores(Long evalId, Long studentId) {
        List<Object[]> rawScores = answerRepo.sumScoreBySubCompetency(evalId);
        Map<Long, Integer> subScoreMap = rawScores.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        Map<Long, Double> upperScoreMap = new HashMap<>();
        List<CoreCptInfo> upperList = infoRepository.findRootCompetencies();

        for (CoreCptInfo upper : upperList) {
            double upperSum = 0.0;
            for (CoreCptInfo sub : upper.getChildren()) {
                int subRawScore = subScoreMap.getOrDefault(sub.getCciId(), 0);
                double subScore100 = (subRawScore / 50.0) * 100.0;
                upperSum += subScore100 * (sub.getWeight() / 100.0);
            }
            upperScoreMap.put(upper.getCciId(), upperSum);
        }

        List<DiagnosisDetailDTO> details = new ArrayList<>();
        double totalSum = 0;
        for (CoreCptInfo upper : upperList) {
            double score = upperScoreMap.getOrDefault(upper.getCciId(), 0.0);
            totalSum += score;
            details.add(DiagnosisDetailDTO.builder()
                    .competencyName(upper.getCciNm())
                    .score((int) Math.round(score))
                    .avgScore(0)
                    .build());
        }

        int totalScore = (int) Math.round(totalSum / upperList.size());
        String levelText = calculateLevelText(totalScore);
        String evalCode = evalRepository.findById(evalId).map(CoreCptEval::getEvalCode).orElse("");

        return new ScoreResult(totalScore, levelText, evalCode, details);
    }

    private String calculateLevelText(int score) {
        if (score >= 90) return "머우 우수";
        if (score >= 80) return "우수";
        if (score >= 70) return "보통";
        return "개선 필요";
    }

    private static class ScoreResult {
        int totalScore;
        String levelText;
        String evalCode;
        List<DiagnosisDetailDTO> details;

        ScoreResult(int totalScore, String levelText, String evalCode, List<DiagnosisDetailDTO> details) {
            this.totalScore = totalScore;
            this.levelText = levelText;
            this.evalCode = evalCode;
            this.details = details;
        }
    }
}

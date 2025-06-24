package kr.ac.dhuniv.core_cpt.service;

import jakarta.transaction.Transactional;
import kr.ac.dhuniv.core_cpt.domain.*;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisDetailDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisStatusResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisAnalysisResponseDTO;

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
     * ✅ 상위 역량별 최신 진단 점수 분석 결과 반환
     * @param studentNo 학번
     * @return 분석 결과 리스트
     */
    public List<DiagnosisAnalysisResponseDTO> getDiagnosisAnalysis(String studentNo) {
        log.info("🔍 상위 역량별 최신 진단 점수 분석 시작 - studentNo: {}", studentNo);
        List<Object[]> raw = answerRepo.findDiagnosisAnalysis(studentNo);

        // 결과 매핑
        List<DiagnosisAnalysisResponseDTO> result = raw.stream()
                .map(row -> new DiagnosisAnalysisResponseDTO(
                        (String) row[0],                      // upper_cci_nm
                        ((Number) row[1]).intValue()          // total_score
                ))
                .collect(Collectors.toList());

        log.info("✅ 분석 결과: {}", result);
        return result;
    }

    public DiagnosisResponseDTO saveDiagnosis(Long cciId, DiagnosisRequestDTO dto) {
        // 1️⃣ 학생 정보 조회
        StdInfo student = stdRepo.findByUserUserId(dto.getStudentUserId())
                .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

        // 2️⃣ 상위 역량 정보 조회
        CoreCptInfo upperCci = infoRepository.findById(cciId)
                .orElseThrow(() -> new IllegalArgumentException("상위역량 정보를 찾을 수 없습니다."));

        // 3️⃣ CoreCptEval 생성 시 상위역량 ID 저장
        CoreCptEval eval = CoreCptEval.builder()
                .answerDate(LocalDateTime.now())
                .evalCode(UUID.randomUUID().toString().substring(0, 8))
                .student(student)
                .upperCompetency(upperCci)  // ✅ 상위역량 정보 연결
                .build();
        evalRepository.save(eval);

        // 4️⃣ 답변 저장
        dto.getAnswers().forEach((qstId, optId) -> {
            CoreCptOptionTemplate option = optRepo.findById(optId)
                    .orElseThrow(() -> new IllegalArgumentException("선택지 정보를 찾을 수 없습니다."));

            CoreCptEvalAnswer answer = CoreCptEvalAnswer.builder()
                    .eval(eval)
                    .question(qstRepo.findById(qstId).orElseThrow(() ->
                            new IllegalArgumentException("문항 정보를 찾을 수 없습니다.")))
                    .selectedOption(option)
                    .answerScore(option.getScore())
                    .build();

            answerRepo.save(answer);
            log.info("✅ 답변 저장 - QstId: {}, OptionId: {}, Score: {}", qstId, optId, option.getScore());
        });

        // 5️⃣ 결과 빌드 (필요하면 cciId 전달)
        ScoreResult result = buildResult(eval.getEvalId(), student.getStdId());

        // 6️⃣ DTO 반환
        return DiagnosisResponseDTO.builder()
                .evalCode(result.evalCode)
                .totalScore(result.totalScore)
                .levelText(result.levelText)
                .details(result.details)
                .build();
    }

    /**
     * 각 상위역량 별 최신 진단 점수를 조회하고 응답 DTO로 변환
     *
     * @param studentId 학생 ID (사용 X, 확장용)
     * @param studentNo 학생 번호 (std_no)
     * @return DiagnosisStatusResponseDTO
     */
    public DiagnosisStatusResponseDTO getLatestDiagnosisByLatestEval(Long studentId, String studentNo) {
        // 최신 진단 데이터 쿼리 실행
        List<Object[]> rows = evalRepository.findLatestScoreByUpperCompetency(studentNo);

        List<DiagnosisDetailDTO> details = new ArrayList<>();  // 상세 역량 점수 리스트
        double totalSum = 0;                                   // 종합 점수 합계

        // 쿼리 결과를 DTO로 변환
        for (Object[] row : rows) {
            String competencyName = (String) row[1];          // 역량명
            int score = ((Number) row[2]).intValue();         // 점수

            // 상세 DTO 추가
            details.add(DiagnosisDetailDTO.builder()
                    .competencyName(competencyName)
                    .score(score)
                    .build());

            totalSum += score;                                // 총점 합산
        }

        // 평균 점수 계산
        int avgScore = details.isEmpty() ? 0 : (int) Math.round(totalSum / details.size());
        String level = calculateLevelText(avgScore);          // 수준 텍스트 계산

        // 최종 응답 객체 생성
        return DiagnosisStatusResponseDTO.builder()
                .totalScore(avgScore)
                .levelText(level)
                .details(details)
                .latestDate(null)                             // 최신 진단일 필요 시 추가 쿼리 필요
                .build();
    }

    /**
     * 점수에 따른 수준 텍스트 반환
     *
     * @param score 평균 점수
     * @return 수준 (매우 우수 / 우수 / 보통 / 개선 필요)
     */
    private String calculateLevelText(int score) {
        if (score >= 90) return "매우 우수";
        if (score >= 80) return "우수";
        if (score >= 70) return "보통";
        return "개선 필요";
    }

    private ScoreResult buildResult(Long evalId, Long studentId) {
        List<Object[]> raw = answerRepo.sumScoreByUpperCompetency(evalId);
        Map<Long, Integer> scoreMap = raw.stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).intValue()));

        List<CoreCptInfo> upperList = infoRepository.findRootCompetencies();
        List<DiagnosisDetailDTO> details = new ArrayList<>();
        double total = 0;
        for (CoreCptInfo upper : upperList) {
            int score = scoreMap.getOrDefault(upper.getCciId(), 0);
            log.info("🔍 상위역량 점수 - {} (ID: {}): {}점", upper.getCciNm(), upper.getCciId(), score);
            details.add(DiagnosisDetailDTO.builder()
                    .competencyName(upper.getCciNm())
                    .score(score)
                    .avgScore(0)
                    .build());
            total += score;
        }
        int avgScore = (int) Math.round(total / upperList.size());
        String level = calculateLevelText(avgScore);
        String evalCode = evalRepository.findById(evalId).map(CoreCptEval::getEvalCode).orElse("");
        log.info("✅ 총점: {}, 레벨: {}", avgScore, level);
        return new ScoreResult(avgScore, level, evalCode, details);
    }

    private DiagnosisStatusResponseDTO buildEmptyStatus() {
        List<CoreCptInfo> upperList = infoRepository.findRootCompetencies();
        List<DiagnosisDetailDTO> details = upperList.stream()
                .map(u -> DiagnosisDetailDTO.builder()
                        .competencyName(u.getCciNm())
                        .score(0)
                        .avgScore(0)
                        .build())
                .collect(Collectors.toList());
        DiagnosisStatusResponseDTO dto = new DiagnosisStatusResponseDTO();
        dto.setTotalScore(null);
        dto.setLevelText(null);
        dto.setLatestDate(null);
        dto.setDetails(details);
        return dto;
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

        DiagnosisStatusResponseDTO toStatus(LocalDate date) {
            DiagnosisStatusResponseDTO dto = new DiagnosisStatusResponseDTO();
            dto.setTotalScore(totalScore);
            dto.setLevelText(levelText);
            dto.setLatestDate(date);
            dto.setDetails(details);
            return dto;
        }
    }
}

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

/**
 * DiagnosisEvalService
 * - 진단 답안을 저장하는 비즈니스 로직
 */
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
     * 진단 답안을 저장하고 평가 코드를 반환
     *
     * @param studentUserId 학번
     * @param cciId         상위역량 ID (현재 로직에서 활용X, 추후 확장 가능)
     * @param answers       {문항ID → 옵션ID}
     * @param elapsedSeconds 소요시간 (현재 저장X, 추후 확장 가능)
     * @return 생성된 평가 코드
     */

    /**
     * ✅ 진단 데이터 저장 + 결과 생성
     * @param cciId 상위 역량 ID
     * @param dto 진단 제출 데이터
     * @return 결과 DTO
     */
    public DiagnosisResponseDTO saveDiagnosis(Long cciId, DiagnosisRequestDTO dto) {
        // 1️⃣ 학생 정보 조회
        StdInfo student = stdRepo.findByUserUserId(dto.getStudentUserId())
                .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

        // 2️⃣ 진단 기본 정보 저장
        CoreCptEval eval = CoreCptEval.builder()
                .answerDate(LocalDateTime.now())
                .evalCode(UUID.randomUUID().toString().substring(0, 8))
                .student(student)
                .build();
        evalRepository.save(eval);
        log.info("✅ 진단 저장: evalId={}, code={}", eval.getEvalId(), eval.getEvalCode());

        // 3️⃣ 답변 저장
        for (Map.Entry<Long, Long> entry : dto.getAnswers().entrySet()) {
            CoreCptOptionTemplate option = optRepo.findById(entry.getValue())
                    .orElseThrow(() -> new IllegalArgumentException("선택지 정보를 찾을 수 없습니다."));

            CoreCptEvalAnswer answer = CoreCptEvalAnswer.builder()
                    .eval(eval)
                    .question(qstRepo.findById(entry.getKey()).orElseThrow())
                    .selectedOption(option)
                    .answerScore(option.getScore()) // ✅ 선택지 점수를 ansScore 로 세팅
                    .build();

            answerRepo.save(answer);
            log.info("✅ 답변 저장: evalId={}, qstId={}, optionId={}, score={}",
                    eval.getEvalId(), entry.getKey(), entry.getValue(), option.getScore());
        }

        // 4️⃣ 점수 집계 (상위 역량별 합산)
        List<Object[]> rawScores = answerRepo.sumScoreByCompetency(eval.getEvalId());
        Map<Long, Integer> scoreMap = rawScores.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        // 5️⃣ 전체 평균
        List<Object[]> rawAvg = answerRepo.avgScoreByCompetency();
        Map<Long, Integer> avgMap = rawAvg.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        // 6️⃣ 상세 DTO 생성
        List<DiagnosisDetailDTO> details = new ArrayList<>();
        for (CoreCptInfo cpt : infoRepository.findRootCompetencies()) {
            int score = scoreMap.getOrDefault(cpt.getCciId(), 0);
            int avg = avgMap.getOrDefault(cpt.getCciId(), 0);
            details.add(DiagnosisDetailDTO.builder()
                    .competencyName(cpt.getCciNm())
                    .score(score)
                    .avgScore(avg)
                    .build());
        }

        // 7️⃣ 종합 점수 + 레벨 계산
        int totalScore = (int) details.stream()
                .mapToInt(DiagnosisDetailDTO::getScore)
                .average()
                .orElse(0);
        String levelText = calculateLevelText(totalScore);

        // 8️⃣ 응답 DTO 반환
        return DiagnosisResponseDTO.builder()
                .evalCode(eval.getEvalCode())
                .totalScore(totalScore)
                .levelText(levelText)
                .details(details)
                .build();
    }

    /**
     * ✅ 점수에 따른 수준 텍스트 반환
     */
    private String calculateLevelText(int score) {
        if (score >= 90) return "매우 우수";
        if (score >= 80) return "우수";
        if (score >= 70) return "보통";
        return "개선 필요";
    }

    /**
     * ✅ 최신 진단 현황 가져오기
     * @param studentId 학생 ID
     * @return 진단 현황 DTO
     */
    public DiagnosisStatusResponseDTO getLatestDiagnosis(Long studentId) {
        // 1️⃣ 상위 핵심역량 목록 조회
        List<CoreCptInfo> competencies = infoRepository.findRootCompetencies();

        // 2️⃣ 학생 점수 조회
        List<Object[]> studentData = evalRepository.findStudentScoresByRootCompetency(studentId);
        Map<Long, Integer> studentScoreMap = new HashMap<>();
        for (Object[] row : studentData) {
            Long id = (Long) row[0];
            Integer sumScore = ((Number) row[2]).intValue();
            studentScoreMap.put(id, sumScore);
        }

        // 3️⃣ 평균 점수 조회
        List<Object[]> avgData = evalRepository.findAvgScoresByRootCompetency();
        Map<Long, Integer> avgScoreMap = new HashMap<>();
        for (Object[] row : avgData) {
            Long id = (Long) row[0];
            Integer avgScore = ((Number) row[1]).intValue();
            avgScoreMap.put(id, avgScore);
        }

        // 4️⃣ 상세 점수 + 진단 여부 확인
        boolean allCompleted = true;
        List<DiagnosisDetailDTO> details = new ArrayList<>();

        for (CoreCptInfo cpt : competencies) {
            int score = studentScoreMap.getOrDefault(cpt.getCciId(), 0);
            int avg = avgScoreMap.getOrDefault(cpt.getCciId(), 0);

            if (!studentScoreMap.containsKey(cpt.getCciId())) {
                allCompleted = false;
            }

            DiagnosisDetailDTO detail = new DiagnosisDetailDTO();
            detail.setCompetencyName(cpt.getCciNm());
            detail.setScore(score);
            detail.setAvgScore(avg);
            details.add(detail);
        }

        // 5️⃣ 종합 점수 계산
        Integer totalScore = null;
        String levelText = null;
        LocalDate latestDate = null;

        if (allCompleted) {
            int sum = details.stream().mapToInt(DiagnosisDetailDTO::getScore).sum();
            totalScore = sum / details.size();
            levelText = calculateLevelText(totalScore);
            latestDate = evalRepository.findLatestDate(studentId);
        }

        // 6️⃣ DTO 반환
        DiagnosisStatusResponseDTO response = new DiagnosisStatusResponseDTO();
        response.setTotalScore(totalScore);
        response.setLevelText(levelText);
        response.setLatestDate(latestDate);
        response.setDetails(details);

        return response;
    }


}

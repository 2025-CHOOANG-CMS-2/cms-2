package kr.ac.dhuniv.core_cpt.service;


import kr.ac.dhuniv.core_cpt.domain.*;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisDetailDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisStatusResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.CompetencyAverageScoreDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.DiagnosisDashboardDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.DiagnosisDetailResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.RecentDiagnosisResponseDto;
import kr.ac.dhuniv.core_cpt.dto.emp_result.DiagnosisDetailResponseBundleDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_result.DiagnosisDetailWithColorDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_result.ProgramRecommendationDTO;
import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisAnalysisResponseDTO;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.transaction.annotation.Transactional;
import kr.ac.dhuniv.core_cpt.repository.*;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    private final CoreCpt_NcsPrgInfoRepository ncsPrgInfoRepository;
    /**
     * ✅ 상위 역량별 최신 진단 점수 분석 결과 반환
     * @param studentNo 학번
     * @return 분석 결과 리스트
     */
    public List<DiagnosisAnalysisResponseDTO> getDiagnosisAnalysis(String studentNo) {
        log.info("🔍 진단 결과 분석 시작 - studentNo: {}", studentNo);

        // 점수 집계
        List<Object[]> raw = answerRepo.findDiagnosisAnalysis(studentNo);  // upper_cci_nm, total_score

        // 상위역량 정보 로딩 (colorHex, 코멘트 매핑 위해)
        List<CoreCptInfo> upperList = infoRepository.findRootCompetencies();

        Map<String, CoreCptInfo> nameToInfo = upperList.stream()
                .collect(Collectors.toMap(CoreCptInfo::getCciNm, Function.identity()));

        List<DiagnosisAnalysisResponseDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            String cciName = (String) row[0];
            int score = ((Number) row[1]).intValue();

            CoreCptInfo info = nameToInfo.get(cciName);
            if (info == null) continue;

            String colorHex = info.getColorHex();
            String comment = info.getCommentTemplates().stream()
                    .filter(t -> score >= t.getMinScore() && score <= t.getMaxScore())
                    .map(CoreCptCommentTemplate::getContent)
                    .findFirst()
                    .orElse(null);

            result.add(new DiagnosisAnalysisResponseDTO(cciName, score, colorHex, comment));
        }

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
    /**
     * ✅ 역량진단 요약 통계 조회 서비스
     */
    @Transactional(readOnly = true)
    public DiagnosisDashboardDTO getDashboardSummary() {
        // 🔹 Repository에서 1행짜리 통계 데이터를 Object[] 리스트로 조회
        List<Object[]> rows = evalRepository.getDiagnosisDashboardRaw();
        // 💡 새로운 평균 점수 계산 (상위역량 기준 진단별 평균)
        Double averageScore = evalRepository.getOverallAverageScoreBasedOnRecentUpperCompetency();
        // 🔸 결과가 존재할 경우 첫 행의 값을 꺼냄
        if (rows != null && !rows.isEmpty()) {
            Object[] result = rows.get(0); // [0]: 전체 진단 수, [1]: 평균 점수, [2]: 문항 수, [3]: 미완료 수

            // 🔍 디버깅용 로그 출력
            log.info("📊 대시보드 쿼리 결과: {}", java.util.Arrays.toString(result));

            // ✅ DTO로 변환 후 반환
            return DiagnosisDashboardDTO.builder()
                    .totalEvalCount(((Number) result[0]).longValue())        // 전체 진단 수
                    .averageScore(averageScore != null ? averageScore : 0.0)        // 평균 점수
                    .questionCount(((Number) result[2]).longValue())         // 문항 수
                    .incompleteStudentCount(((Number) result[3]).longValue())// 미완료 학생 수
                    .build();
        }

        // ❗ 데이터가 없거나 쿼리 실패 시 기본값 반환
        return DiagnosisDashboardDTO.builder()
                .totalEvalCount(0L)
                .averageScore(0.0)
                .questionCount(0L)
                .incompleteStudentCount(0L)
                .build();
    }

    /**
     * ✅ 상위역량별 평균 점수 + 색상 정보 조회
     * - 모든 학생의 가장 최근 진단을 기반으로 계산
     * - Chart.js 시각화를 위한 데이터 구성
     *
     * @return List<CompetencyAverageScoreDTO>
     */
    @Transactional(readOnly = true)
    public List<CompetencyAverageScoreDTO> getAverageScoreByCompetency() {
        List<Object[]> rawList = evalRepository.getAvgScoreByUpperCompetencyWithColor();

        List<CompetencyAverageScoreDTO> result = new ArrayList<>();

        for (Object[] row : rawList) {
            CompetencyAverageScoreDTO dto = CompetencyAverageScoreDTO.builder()
                    .upperCciId(((Number) row[0]).longValue())           // 상위 역량 ID
                    .upperCciName((String) row[1])                      // 상위 역량 이름
                    .colorHex((String) row[2])                          // 색상 HEX 코드
                    .avgScore(((Number) row[3]).doubleValue())          // 평균 점수
                    .build();

            result.add(dto);
        }

        return result;
    }
    // 📌 3. Service 메서드
    public List<RecentDiagnosisResponseDto> getRecentDiagnosisResults() {
        List<Object[]> rows = evalRepository.findRecentDiagnosisResultsRaw();
        List<RecentDiagnosisResponseDto> results = new ArrayList<>();

        for (Object[] row : rows) {
            results.add(new RecentDiagnosisResponseDto(
                    (String) row[0],                     // stdNo
                    (String) row[1],                     // stdNm
                    (String) row[2],                     // deptName
                    (Integer) row[3],                     // grade
                    (String) row[4],                     // diagnosisDate
                    row[5] != null ? ((Number) row[5]).doubleValue() : null // averageScore
            ));
        }

        return results;
    }
    public List<DiagnosisDetailResponseDTO> getDiagnosisDetail(String stdNo) {
        List<Object[]> rows = evalRepository.findDiagnosisDetailByStdNo(stdNo);
        List<DiagnosisDetailResponseDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new DiagnosisDetailResponseDTO(
                    (String) row[0],
                    row[1] != null ? ((Number) row[1]).doubleValue() : null,
                    (String) row[2]
            ));
        }
        return result;
    }

    public DiagnosisDetailResponseBundleDTO getDiagnosisDetailWithStudentInfo(String stdNo) {
        // 학생 정보 가져오기
        StdInfo student = stdRepo.findByUser_UserId(stdNo)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));

        // 진단 점수 리스트 가져오기
        List<Object[]> rows = evalRepository.findDiagnosisDetailWithColorByStdNo(stdNo);
        List<DiagnosisDetailWithColorDTO> detailList = new ArrayList<>();

        for (Object[] row : rows) {
            detailList.add(new DiagnosisDetailWithColorDTO(
                    (String) row[0],                      // 역량명
                    row[1] != null ? ((Number) row[1]).doubleValue() : null, // 점수
                    (String) row[2]                       // colorHex
            ));
        }

        return new DiagnosisDetailResponseBundleDTO(
                stdNo,
                student.getStdNm(),
                student.getScsbjtCd(),     // 학과명 (혹은 별도 테이블에서 학과명 매핑 필요)
                student.getSchoolYear(),
                detailList
        );
    }

    public List<ProgramRecommendationDTO> getRecommendedPrograms(String stdNo) {
        List<NcsPrgInfo> programs = ncsPrgInfoRepository.findTop2RecommendedByWeakestCompetency(stdNo);

        return programs.stream()
                .map(p -> new ProgramRecommendationDTO(
                        p.getPrgId(),
                        p.getPrgNm(),
                        p.getPrgDesc(),
                        p.getCategoryName(),      // ← CoreCptInfo.cciNm 반환됨
                        p.getAplyEndDate(),
                        p.getImageUrl()
                ))
                .toList();
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

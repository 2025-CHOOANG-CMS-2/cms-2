package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.RecentDiagnosisResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ✅ CoreCptEvalRepository
 * - CoreCptEval 저장용 리포지토리
 */

public interface CoreCptEvalRepository extends JpaRepository<CoreCptEval, Long> {

    Optional<Long> findFirstByStudent_StdIdOrderByAnswerDateDesc(Long studentId);
    List<CoreCptEval> findByStudentStdId(Long stdNo);
    boolean existsByEvalCode(String evalCode);

   /* *//**
     * ✅ 학생 상위 핵심역량별 점수 합계 조회
     * - CoreCptEvalAnswer → CoreCptQst → CoreCptInfo → CoreCptOptionTemplate 점수
     *//*
    @Query("SELECT q.coreCptInfo.cciId, q.coreCptInfo.cciNm, SUM(opt.score) " +
            "FROM CoreCptEvalAnswer a " +
            "JOIN a.eval e " +                      // 진단 정보 (학생 ID 필터용)
            "JOIN a.question q " +                       // 문항
            "JOIN a.question.optionTemplate opt " +          // 선택지 점수
            "WHERE e.student.stdId = :studentId " +
            "AND q.coreCptInfo.parent IS NULL " +          // 상위 핵심역량
            "GROUP BY q.coreCptInfo.cciId, q.coreCptInfo.cciNm")
    List<Object[]> findStudentScoresByRootCompetency(Long studentId);

    *//**
     * ✅ 상위 핵심역량별 전체 평균 점수 조회
     * - 모든 학생의 진단 답변 기반
     *//*
    @Query("SELECT q.coreCptInfo.cciId, AVG(opt.score) " +
            "FROM CoreCptEvalAnswer a " +
            "JOIN a.question q " +                       // 문항
            "JOIN a.question.optionTemplate opt " +          // 선택지 점수
            "WHERE q.coreCptInfo.parent IS NULL " +        // 상위 핵심역량
            "GROUP BY q.coreCptInfo.cciId")
    List<Object[]> findAvgScoresByRootCompetency();

    *//**
     * ✅ 최신 진단일 조회 (학생 기준)
     *//*
    @Query("SELECT MAX(e.answerDate) FROM CoreCptEval e WHERE e.student.stdId = :studentId")
    LocalDate findLatestDate(Long studentId);
*/

    /**
     * 학생 번호(std_no) 기준으로
     * 각 상위역량별 최신 진단(eval_id) 점수 합계를 조회
     *
     * @param studentNo 학생번호 (std_no)
     * @return Object[]: [upper_cci_id(Long), upper_cci_nm(String), total_score(Integer)]
     */
    @Query(value = """
        WITH latest_eval AS (
            SELECT 
                COALESCE(p.cci_id, c.cci_id) AS upper_cci_id,
                MAX(e.eval_id) AS latest_eval_id
            FROM core_cpt_eval e
            JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
            JOIN core_cpt_qst q ON a.qst_id = q.qst_id
            JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
            LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
            WHERE e.std_no = :studentNo
            GROUP BY COALESCE(p.cci_id, c.cci_id)
        )
        SELECT 
            COALESCE(p.cci_id, c.cci_id) AS upper_cci_id,
            COALESCE(p.cci_nm, c.cci_nm) AS upper_cci_nm,
            SUM(a.ans_score) AS total_score
        FROM core_cpt_eval e
        JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
        JOIN core_cpt_qst q ON a.qst_id = q.qst_id
        JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
        LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
        JOIN latest_eval le ON le.upper_cci_id = COALESCE(p.cci_id, c.cci_id) AND le.latest_eval_id = e.eval_id
        WHERE e.std_no = :studentNo
        GROUP BY COALESCE(p.cci_id, c.cci_id), COALESCE(p.cci_nm, c.cci_nm)
        ORDER BY upper_cci_id
        """, nativeQuery = true)
    List<Object[]> findLatestScoreByUpperCompetency(@Param("studentNo") String studentNo);


    /**
     * ✅ 최신 진단 ID 기준 상위역량별 점수 집계 (중복 상위역량 제거, 최신 eval_id 우선)
     * @param studentNo 학생 학번
     * @return Object[] : { eval_id(Long), upper_cci_id(Long), competency_name(String), color_hex(String), total_score(Long) }
     */
    @Query(value =
            "WITH eval_with_competency AS ( " +
                    "  SELECT e.eval_id, " +
                    "         COALESCE(p.cci_id, c.cci_id) AS upper_cci_id, " +
                    "         COALESCE(p.cci_nm, c.cci_nm) AS competency_name, " +
                    "         COALESCE(p.color_hex, c.color_hex) AS color_hex, " +
                    "         SUM(a.ans_score) AS total_score, " +
                    "         ROW_NUMBER() OVER ( " +
                    "           PARTITION BY COALESCE(p.cci_id, c.cci_id) " +
                    "           ORDER BY e.eval_id DESC " +
                    "         ) AS rn " +
                    "  FROM core_cpt_eval e " +
                    "  JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id " +
                    "  JOIN core_cpt_qst q ON a.qst_id = q.qst_id " +
                    "  JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id " +
                    "  LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id " +
                    "  WHERE e.std_no = :studentNo " +
                    "  GROUP BY e.eval_id, COALESCE(p.cci_id, c.cci_id), COALESCE(p.cci_nm, c.cci_nm), COALESCE(p.color_hex, c.color_hex) " +
                    ") " +
                    "SELECT eval_id, upper_cci_id, competency_name, color_hex, total_score " +
                    "FROM eval_with_competency " +
                    "WHERE rn = 1 " +
                    "ORDER BY competency_name",
            nativeQuery = true)
    List<Object[]> findLatestCompetencyScores(@Param("studentNo") String studentNo);


    /**
     * ✅ 역량진단 대시보드 요약 통계 조회
     * - 총 진단 수
     * - 평균 점수
     * - 전체 문항 수
     * - 미완료 학생 수
     * → 한 줄짜리 row로 결과 반환
     *
     * @return List<Object[]> : [totalEvalCount, averageScore, questionCount, incompleteStudentCount]
     */
    @Query(value = """
        SELECT
            (SELECT COUNT(*) FROM core_cpt_eval) AS totalEvalCount,
            (SELECT ROUND(AVG(ans_score), 1) FROM core_cpt_eval_answer) AS averageScore,
            (SELECT COUNT(*) FROM core_cpt_qst) AS questionCount,
            (
                SELECT COUNT(*)
                FROM std_info s
                LEFT JOIN core_cpt_eval e ON s.std_no = e.std_no
                WHERE e.eval_id IS NULL AND s.use_yn = 'Y'
            ) AS incompleteStudentCount
        """, nativeQuery = true)
    List<Object[]> getDiagnosisDashboardRaw();


    @Query(value = """
    SELECT 
        ROUND(AVG(avg_score), 1) AS avg_all_students_score
    FROM (
        SELECT 
            std_no,
            ROUND(SUM(total_score) * 1.0 / COUNT(*), 1) AS avg_score
        FROM (
            SELECT 
                e.std_no,
                COALESCE(p.cci_id, c.cci_id) AS upper_cci_id,
                SUM(a.ans_score) AS total_score
            FROM core_cpt_eval e
            JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
            JOIN core_cpt_qst q ON a.qst_id = q.qst_id
            JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
            LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
            WHERE e.eval_id IN (
                SELECT MAX(e2.eval_id)
                FROM core_cpt_eval e2
                JOIN core_cpt_eval_answer a2 ON e2.eval_id = a2.eval_id
                JOIN core_cpt_qst q2 ON a2.qst_id = q2.qst_id
                JOIN core_cpt_info c2 ON q2.core_cpt_info_id = c2.cci_id
                LEFT JOIN core_cpt_info p2 ON c2.parent_id = p2.cci_id
                GROUP BY e2.std_no, COALESCE(p2.cci_id, c2.cci_id)
            )
            GROUP BY e.std_no, COALESCE(p.cci_id, c.cci_id)
        ) student_scores
        GROUP BY std_no
    ) per_student_avg
""", nativeQuery = true)
    Double getOverallAverageScoreBasedOnRecentUpperCompetency();


    @Query(value = """

            SELECT\s
    recent_scores.upper_cci_id,
    recent_scores.upper_cci_name,
    recent_scores.color_hex,
    ROUND(AVG(recent_scores.score_sum), 1) AS avg_score
FROM (
    SELECT\s
        COALESCE(p.cci_id, c.cci_id) AS upper_cci_id,
        COALESCE(p.cci_nm, c.cci_nm) AS upper_cci_name,
        COALESCE(p.color_hex, c.color_hex) AS color_hex,
        e.std_no,
        SUM(a.ans_score) AS score_sum
    FROM core_cpt_eval e
    JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
    JOIN core_cpt_qst q ON a.qst_id = q.qst_id
    JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
    LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
    WHERE e.eval_id IN (
        SELECT MAX(e2.eval_id)
        FROM core_cpt_eval e2
        JOIN core_cpt_eval_answer a2 ON e2.eval_id = a2.eval_id
        JOIN core_cpt_qst q2 ON a2.qst_id = q2.qst_id
        JOIN core_cpt_info c2 ON q2.core_cpt_info_id = c2.cci_id
        LEFT JOIN core_cpt_info p2 ON c2.parent_id = p2.cci_id
        GROUP BY e2.std_no, COALESCE(p2.cci_id, c2.cci_id)
    )
    GROUP BY\s
        e.std_no,
        COALESCE(p.cci_id, c.cci_id),
        COALESCE(p.cci_nm, c.cci_nm),
        COALESCE(p.color_hex, c.color_hex)
) recent_scores
GROUP BY\s
    recent_scores.upper_cci_id,
    recent_scores.upper_cci_name,
    recent_scores.color_hex
ORDER BY\s
    recent_scores.upper_cci_id;
""", nativeQuery = true)
    List<Object[]> getAvgScoreByUpperCompetencyWithColor();


    @Query(value = """
    SELECT 
        s.std_no AS stdNo,
        s.std_nm AS stdNm,
        s.scsbjt_cd AS deptName,
        s.sch_yr AS grade,
        TO_CHAR(MAX(e.ans_dt), 'YYYY-MM-DD HH24:MI') AS diagnosisDate,
        ROUND(SUM(score_data.total_score) * 1.0 / COUNT(score_data.upper_cci_id), 1) AS averageScore
    FROM std_info s
    JOIN core_cpt_eval e ON s.std_no = e.std_no
    JOIN (
        SELECT 
            e2.eval_id,
            e2.std_no,
            COALESCE(p2.cci_id, c2.cci_id) AS upper_cci_id,
            SUM(a2.ans_score) AS total_score
        FROM core_cpt_eval e2
        JOIN core_cpt_eval_answer a2 ON e2.eval_id = a2.eval_id
        JOIN core_cpt_qst q2 ON a2.qst_id = q2.qst_id
        JOIN core_cpt_info c2 ON q2.core_cpt_info_id = c2.cci_id
        LEFT JOIN core_cpt_info p2 ON c2.parent_id = p2.cci_id
        WHERE e2.eval_id IN (
            SELECT MAX(e3.eval_id)
            FROM core_cpt_eval e3
            JOIN core_cpt_eval_answer a3 ON e3.eval_id = a3.eval_id
            JOIN core_cpt_qst q3 ON a3.qst_id = q3.qst_id
            JOIN core_cpt_info c3 ON q3.core_cpt_info_id = c3.cci_id
            LEFT JOIN core_cpt_info p3 ON c3.parent_id = p3.cci_id
            GROUP BY e3.std_no, COALESCE(p3.cci_id, c3.cci_id)
        )
        GROUP BY e2.eval_id, e2.std_no, COALESCE(p2.cci_id, c2.cci_id)
    ) score_data ON e.eval_id = score_data.eval_id
    GROUP BY s.std_no, s.std_nm, s.scsbjt_cd, s.sch_yr
""", nativeQuery = true)//학생들의 최근 핵심역량 진단 결과 목록을 조회하는 기능
    List<Object[]> findRecentDiagnosisResultsRaw();
    @Query(value = """
    SELECT 
        COALESCE(p.cci_nm, c.cci_nm) AS upperCciName,
        SUM(a.ans_score) AS score,
        TO_CHAR(MAX(e.ans_dt), 'YYYY-MM-DD HH24:MI') AS answeredAt
    FROM core_cpt_eval e
    JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
    JOIN core_cpt_qst q ON a.qst_id = q.qst_id
    JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
    LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
    WHERE e.std_no = :stdNo
    GROUP BY COALESCE(p.cci_nm, c.cci_nm)
    ORDER BY upperCciName
""", nativeQuery = true)
    List<Object[]> findDiagnosisDetailByStdNo(@Param("stdNo") String stdNo);


    @Query(value = """
   SELECT\s
       COALESCE(p.cci_nm, c.cci_nm) AS upperCciName,
       SUM(a.ans_score) AS score,
       COALESCE(p.color_hex, c.color_hex) AS colorHex
   FROM core_cpt_eval e
   JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
   JOIN core_cpt_qst q ON a.qst_id = q.qst_id
   JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
   LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
   WHERE e.eval_id IN (
       SELECT MAX(e2.eval_id)
       FROM core_cpt_eval e2
       JOIN core_cpt_eval_answer a2 ON e2.eval_id = a2.eval_id
       JOIN core_cpt_qst q2 ON a2.qst_id = q2.qst_id
       JOIN core_cpt_info c2 ON q2.core_cpt_info_id = c2.cci_id
       LEFT JOIN core_cpt_info p2 ON c2.parent_id = p2.cci_id
       WHERE e2.std_no = :stdNo
       GROUP BY COALESCE(p2.cci_id, c2.cci_id)
   )
   AND e.std_no = :stdNo
   GROUP BY COALESCE(p.cci_nm, c.cci_nm), COALESCE(p.color_hex, c.color_hex)
   ORDER BY upperCciName
   
""", nativeQuery = true)
    List<Object[]> findDiagnosisDetailWithColorByStdNo(@Param("stdNo") String stdNo);
}
package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
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

    /**
     * ✅ 학생 상위 핵심역량별 점수 합계 조회
     * - CoreCptEvalAnswer → CoreCptQst → CoreCptInfo → CoreCptOptionTemplate 점수
     */
    @Query("SELECT q.coreCptInfo.cciId, q.coreCptInfo.cciNm, SUM(opt.score) " +
            "FROM CoreCptEvalAnswer a " +
            "JOIN a.eval e " +                      // 진단 정보 (학생 ID 필터용)
            "JOIN a.question q " +                       // 문항
            "JOIN a.question.optionTemplate opt " +          // 선택지 점수
            "WHERE e.student.stdId = :studentId " +
            "AND q.coreCptInfo.parent IS NULL " +          // 상위 핵심역량
            "GROUP BY q.coreCptInfo.cciId, q.coreCptInfo.cciNm")
    List<Object[]> findStudentScoresByRootCompetency(Long studentId);

    /**
     * ✅ 상위 핵심역량별 전체 평균 점수 조회
     * - 모든 학생의 진단 답변 기반
     */
    @Query("SELECT q.coreCptInfo.cciId, AVG(opt.score) " +
            "FROM CoreCptEvalAnswer a " +
            "JOIN a.question q " +                       // 문항
            "JOIN a.question.optionTemplate opt " +          // 선택지 점수
            "WHERE q.coreCptInfo.parent IS NULL " +        // 상위 핵심역량
            "GROUP BY q.coreCptInfo.cciId")
    List<Object[]> findAvgScoresByRootCompetency();

    /**
     * ✅ 최신 진단일 조회 (학생 기준)
     */
    @Query("SELECT MAX(e.answerDate) FROM CoreCptEval e WHERE e.student.stdId = :studentId")
    LocalDate findLatestDate(Long studentId);


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


}
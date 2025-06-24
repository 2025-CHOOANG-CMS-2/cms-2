package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEvalAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoreCptEvalAnswerRepository extends JpaRepository<CoreCptEvalAnswer, Long> {
    List<CoreCptEvalAnswer> findByEval_EvalCode(String evalCode);

    /**
     * ✅ 상위 역량별 최신 진단 점수 집계
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
            COALESCE(p.cci_nm, c.cci_nm) AS upper_cci_nm,
            SUM(a.ans_score) AS total_score
        FROM core_cpt_eval e
        JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
        JOIN core_cpt_qst q ON a.qst_id = q.qst_id
        JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
        LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
        JOIN latest_eval le 
          ON le.upper_cci_id = COALESCE(p.cci_id, c.cci_id) 
         AND le.latest_eval_id = e.eval_id
        WHERE e.std_no = :studentNo
        GROUP BY COALESCE(p.cci_nm, c.cci_nm)
        ORDER BY upper_cci_nm
        """, nativeQuery = true)
    List<Object[]> findDiagnosisAnalysis(@Param("studentNo") String studentNo);
    /**
     * ✅ 하위역량별 점수 합계 (각 하위역량의 문항 점수 합산)
     */
    @Query("""
            SELECT c.cciId, SUM(opt.score)
            FROM CoreCptEvalAnswer a
            JOIN a.question q
            JOIN q.coreCptInfo c
            JOIN a.selectedOption opt
            WHERE a.eval.evalId = :evalId
            GROUP BY c.cciId
            """)
    List<Object[]> sumScoreBySubCompetency(@Param("evalId") Long evalId);

    @Query("SELECT q.coreCptInfo.cciId, AVG(opt.score) " +
            "FROM CoreCptEvalAnswer a " +
            "JOIN a.question q " +
            "JOIN a.selectedOption opt " +
            "WHERE q.coreCptInfo.parent IS NULL " +
            "GROUP BY q.coreCptInfo.cciId")
    List<Object[]> avgScoreByCompetency();

    @Query(value = """
        SELECT p.cci_id AS upper_id, SUM(opt.score) AS sum
        FROM core_cpt_eval_answer a
        JOIN core_cpt_qst q ON a.qst_id = q.qst_id
        JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
        JOIN core_cpt_info p ON c.parent_id = p.cci_id
        JOIN core_cpt_option_template opt ON a.option_id = opt.option_id
        WHERE a.eval_id = :evalId
        GROUP BY p.cci_id
        """, nativeQuery = true)
    List<Object[]> sumScoreByUpperCompetency(@Param("evalId") Long evalId);
}

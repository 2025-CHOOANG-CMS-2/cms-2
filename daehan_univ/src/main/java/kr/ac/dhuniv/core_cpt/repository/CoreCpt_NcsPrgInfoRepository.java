package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoreCpt_NcsPrgInfoRepository  extends JpaRepository<NcsPrgInfo, Long> {
    /**
     * ✅ 핵심역량별 프로그램 조회
     */
    List<NcsPrgInfo> findByCoreCpt(CoreCptInfo coreCpt);

    /**
     * 해당 프로그램의 신청자 수를 집계하는 메서드
     *
     * @param prgId 비교과 프로그램 ID
     * @return 신청자 수
     */
    @Query("SELECT COUNT(a) FROM NcsPrgAply a WHERE a.program.prgId = :prgId")
    int countAppliedStudents(Long prgId);


    @Query(value = """
    WITH latest_score AS (
        SELECT 
            COALESCE(p.cci_id, c.cci_id) AS upper_cci_id,
            COALESCE(p.cci_nm, c.cci_nm) AS upper_cci_name,
            SUM(a.ans_score) AS total_score
        FROM core_cpt_eval e
        JOIN core_cpt_eval_answer a ON e.eval_id = a.eval_id
        JOIN core_cpt_qst q ON a.qst_id = q.qst_id
        JOIN core_cpt_info c ON q.core_cpt_info_id = c.cci_id
        LEFT JOIN core_cpt_info p ON c.parent_id = p.cci_id
        WHERE e.eval_id IN (
            SELECT MAX(e2.eval_id)
            FROM core_cpt_eval e2
            WHERE e2.std_no = :stdNo
        )
        GROUP BY COALESCE(p.cci_id, c.cci_id), COALESCE(p.cci_nm, c.cci_nm)
        ORDER BY total_score ASC
        LIMIT 1
    )
    SELECT p.*
    FROM ncs_prg_info p
    JOIN latest_score s ON p.cci_id = s.upper_cci_id
    ORDER BY p.aply_end_ymd DESC
    LIMIT 2
""", nativeQuery = true)
    List<NcsPrgInfo> findTop2RecommendedByWeakestCompetency(@Param("stdNo") String stdNo);
}

package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEvalAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoreCptEvalAnswerRepository extends JpaRepository<CoreCptEvalAnswer, Long> {
    List<CoreCptEvalAnswer> findByEval_EvalCode(String evalCode);
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
}

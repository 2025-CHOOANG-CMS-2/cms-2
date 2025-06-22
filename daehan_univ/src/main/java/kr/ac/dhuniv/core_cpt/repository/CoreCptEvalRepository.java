package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ✅ CoreCptEvalRepository
 * - CoreCptEval 저장용 리포지토리
 */

public interface CoreCptEvalRepository extends JpaRepository<CoreCptEval, Long> {
    @Query("""
    SELECT e.evalId FROM CoreCptEval e
    WHERE e.student.stdId = :studentId
    ORDER BY e.answerDate DESC
    LIMIT 1
    """)
    Optional<Long> findLatestEvalIdByStudent(Long studentId);
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
}
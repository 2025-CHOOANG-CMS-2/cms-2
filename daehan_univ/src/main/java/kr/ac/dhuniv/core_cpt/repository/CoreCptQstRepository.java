package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ✅ CoreCptQstRepository
 * - 문항 데이터베이스 접근 레이어
 */
public interface CoreCptQstRepository extends JpaRepository<CoreCptQst, Long> {
    // 특정 역량 엔티티에 속한 문항만 조회
    List<CoreCptQst> findByCoreCptInfo(CoreCptInfo info);
    /**
     * ✅ 하위 역량별 최대 문항 코드 조회
     *
     * @param subCptId 하위 역량 ID
     * @return 최대 코드
     */
    @Query("SELECT MAX(q.qstCode) FROM CoreCptQst q WHERE q.coreCptInfo.cciId = :subCptId")
    String findMaxQstCodeBySubCpt(@Param("subCptId") Long subCptId);


    /**
     * ✅ 조건 + 페이징 쿼리
     *
     * @param topCptId 상위 역량 ID
     * @param subCptId 하위 역량 ID
     * @param keyword 검색어
     * @param pageable 페이징
     * @return Page<CoreCptQst>
     */
    @Query("""
        SELECT q FROM CoreCptQst q
        WHERE (:topCptId IS NULL OR q.coreCptInfo.parent.cciId = :topCptId)
        AND (:subCptId IS NULL OR q.coreCptInfo.cciId = :subCptId)
        AND (:keyword IS NULL OR q.qstCont LIKE %:keyword%)
    """)
    Page<CoreCptQst> filter(
            @Param("topCptId") Long topCptId,
            @Param("subCptId") Long subCptId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    // ✅ 특정 역량에 속한 문항 목록 조회 (문항 순서 기준)
    List<CoreCptQst> findByCoreCptInfoOrderByQstOrdAsc(CoreCptInfo coreCptInfo);
}

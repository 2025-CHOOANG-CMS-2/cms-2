package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoreCptQstRepository extends JpaRepository<CoreCptQst, Long> {

    /**
     * ✅ 해당 하위 역량의 최대 문항 코드 반환
     * @param subCptId 하위 역량 ID
     * @return 최대 문항 코드 (문자열)
     */
    @Query("SELECT MAX(q.qstCode) FROM CoreCptQst q WHERE q.coreCptInfo.id = :subCptId")
    String findMaxQstCodeBySubCpt(@Param("subCptId") Long subCptId);
}

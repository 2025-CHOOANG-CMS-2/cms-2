package kr.ac.dhuniv.admin.repository;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminEmpRepository extends JpaRepository<EmplInfo, Long>, JpaSpecificationExecutor<EmplInfo> {

    Optional<EmplInfo> findByEmplEmailAddr(String emplEmailAddr);
    Optional<EmplInfo> findByEmplTelno(String emplTelno);
    Optional<EmplInfo> findByEmplNo(String emplNo);

    // 기존 EMPxxx 형식의 최대 숫자 부분 조회 (이젠 사용하지 않을 수 있음)
    @Query("SELECT MAX(CAST(SUBSTRING(e.emplNo, 4) AS int)) FROM EmplInfo e WHERE e.emplNo LIKE 'EMP%'")
    Optional<Integer> findMaxNumericEmplNoPart();

    // 새로운 사번 체계 (YYYYDDDSSS)에서 특정 년도와 부서 코드에 대한 최대 순번 조회
    @Query("SELECT MAX(CAST(SUBSTRING(e.emplNo, 8) AS int)) FROM EmplInfo e WHERE SUBSTRING(e.emplNo, 1, 4) = :year AND SUBSTRING(e.emplNo, 5, 3) = :deptCd")
    Optional<Integer> findMaxSequenceForEmployeeId(@Param("year") String year, @Param("deptCd") String deptCd);
}

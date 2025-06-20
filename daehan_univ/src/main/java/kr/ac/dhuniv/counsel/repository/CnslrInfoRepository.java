package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnslrInfo;
import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CnslrInfoRepository extends JpaRepository<CnslrInfo, Long> {

    // [최종 수정] 모든 JPQL 쿼리의 JOIN 방식을 명시적 ON 절로 변경
    
    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorListDto(" +
           "  ci.employee.user.userId, " +
           "  ci.employee.emplNm, " +
           "  ci.employee.emplEmailAddr, " +
           "  ci.employee.emplTelno, " +
           "  ci.cnslSpec, " +
           "  CASE WHEN ci.isActive = true THEN 'active' ELSE 'inactive' END, " +
           "  ci.intro, " +
           "  COUNT(DISTINCT ca.id), " +
           "  COALESCE(AVG(cr.satisfactionScore), 0.0)" +
           ") " +
           "FROM CnslrInfo ci " +
           // ci.employee와 ca.employee가 동일한 EmplInfo 객체인 경우를 조인
           "LEFT JOIN CnslAply ca ON ci.employee = ca.employee " +
           // ca와 cr.counselingApplication이 동일한 CnslAply 객체인 경우를 조인
           "LEFT JOIN CnslRslt cr ON ca = cr.counselingApplication " +
           "WHERE ci.isActive = true " +
           "AND (:specialty = 'all' OR ci.cnslSpec = :specialty) " +
           "AND (:counselorId = 'all' OR ci.employee.user.userId = :counselorId) " +
           "GROUP BY ci.cnslrId, ci.employee.emplNm, ci.employee.emplEmailAddr, ci.employee.emplTelno, ci.cnslSpec, ci.intro, ci.isActive")
    List<CounselorListDto> findActiveCounselorsByFilter(@Param("specialty") String specialty, @Param("counselorId") String counselorId);

    
    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorSimpleDto(ci.employee.user.userId, ci.employee.emplNm) " +
           "FROM CnslrInfo ci " +
           "WHERE ci.isActive = true AND (:specialty = 'all' OR ci.cnslSpec = :specialty)")
    List<CounselorSimpleDto> findSimpleActiveCounselorsBySpecialty(@Param("specialty") String specialty);


    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorListDto(" +
           "  ci.employee.user.userId, ci.employee.emplNm, ci.employee.emplEmailAddr, ci.employee.emplTelno, " +
           "  ci.cnslSpec, CASE WHEN ci.isActive = true THEN 'active' ELSE 'inactive' END, ci.intro, " +
           "  COUNT(DISTINCT ca.id), COALESCE(AVG(cr.satisfactionScore), 0.0)" +
           ") " +
           "FROM CnslrInfo ci " +
           "LEFT JOIN CnslAply ca ON ci.employee = ca.employee " +
           "LEFT JOIN CnslRslt cr ON ca = cr.counselingApplication " +
           "WHERE ci.employee.user.userId = :userId " +
           "GROUP BY ci.cnslrId, ci.employee.emplNm, ci.employee.emplEmailAddr, ci.employee.emplTelno, ci.cnslSpec, ci.intro, ci.isActive")
    Optional<CounselorListDto> findCounselorDetailByUserId(@Param("userId") String userId);
    
    
    Optional<CnslrInfo> findByEmployee_User_UserId(String userId);

}
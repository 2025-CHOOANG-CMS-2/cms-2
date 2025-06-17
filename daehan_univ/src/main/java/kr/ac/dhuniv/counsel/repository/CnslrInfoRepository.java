package kr.ac.dhuniv.counsel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.counsel.domain.CnslrInfo;
import kr.ac.dhuniv.counsel.dto.CounselorListDto;

@Repository
public interface CnslrInfoRepository extends JpaRepository<CnslrInfo, Long> {

    // [최종 수정된 쿼리 1: 목록 조회]
    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorListDto(" +
           "  ci.emplNo, " +
           "  ei.emplNm, " +
           "  ei.emplEmailAddr, " +
           "  ei.emplTelno, " +
           "  ci.cnslSpec, " +
           "  CASE WHEN ci.isActive = true THEN 'active' ELSE 'inactive' END, " +
           "  ci.intro, " + // [수정] intro 필드 추가
           "  COUNT(DISTINCT cr.cnslRsltId), " +
           "  COALESCE(AVG(cr.satisfactionScore), 0.0)" + 
           ") " +
           "FROM CnslrInfo ci " +
           "JOIN EmplInfo ei ON ci.emplNo = ei.emplNo " +
           "LEFT JOIN CnslAply ca ON ci.emplNo = ca.employee.emplNo " +
           "LEFT JOIN CnslRslt cr ON ca.cnslAplyId = cr.counselingApplication.cnslAplyId " +
           "GROUP BY ci.emplNo, ei.emplNm, ei.emplEmailAddr, ei.emplTelno, ci.cnslSpec, ci.isActive, ci.intro") // [수정] GROUP BY 에 intro 추가
    List<CounselorListDto> findCounselorList();
    
    // [최종 수정된 쿼리 2: 상세 조회]
    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorListDto(" +
           "  ci.emplNo, ei.emplNm, ei.emplEmailAddr, ei.emplTelno, " +
           "  ci.cnslSpec, CASE WHEN ci.isActive = true THEN 'active' ELSE 'inactive' END, " +
           "  ci.intro, " + // [수정] intro 필드 추가
           "  COUNT(DISTINCT cr.cnslRsltId), COALESCE(AVG(cr.satisfactionScore), 0.0)" +
           ") " +
           "FROM CnslrInfo ci " +
           "JOIN EmplInfo ei ON ci.emplNo = ei.emplNo " +
           "LEFT JOIN CnslAply ca ON ci.emplNo = ca.employee.emplNo " +
           "LEFT JOIN CnslRslt cr ON ca.cnslAplyId = cr.counselingApplication.cnslAplyId " +
           "WHERE ci.emplNo = :emplNo " +
           "GROUP BY ci.emplNo, ei.emplNm, ei.emplEmailAddr, ei.emplTelno, ci.cnslSpec, ci.isActive, ci.intro") // [수정] GROUP BY 에 intro 추가
    Optional<CounselorListDto> findCounselorDetailByEmplNo(@Param("emplNo") String emplNo);

    // 수정 기능을 위한 메소드
    Optional<CnslrInfo> findByEmplNo(String emplNo);
    
}
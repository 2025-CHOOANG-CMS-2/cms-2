package kr.ac.dhuniv.counsel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import kr.ac.dhuniv.counsel.domain.CnslrInfo;
import kr.ac.dhuniv.counsel.dto.CounselorListDto;

public interface CnslrInfoRepository extends JpaRepository<CnslrInfo, Long> {
	@Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorListDto(" +
           "  ci.emplNo, " +
           "  ei.emplNm, " +
           "  ei.emplEmailAddr, " +
           "  ei.emplTelno, " +
           "  ci.cnslSpec, " +
           "  CASE WHEN ci.isActive = true THEN 'active' ELSE 'inactive' END, " +
           "  COUNT(DISTINCT cr.cnslRsltId), " +
           "  COALESCE(AVG(cr.satisfactionScore), 0.0)" + 
           ") " +
           "FROM CnslrInfo ci " +
           "JOIN EmplInfo ei ON ci.emplNo = ei.emplNo " +
           "LEFT JOIN CnslAply ca ON ci.emplNo = ca.employee.emplNo " +
           "LEFT JOIN CnslRslt cr ON ca.cnslAplyId = cr.counselingApplication.cnslAplyId " +
           "GROUP BY ci.emplNo, ei.emplNm, ei.emplEmailAddr, ei.emplTelno, ci.cnslSpec, ci.isActive")
	List<CounselorListDto> findCounselorList(); 
}

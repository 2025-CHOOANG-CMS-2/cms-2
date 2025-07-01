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

    // [수정] 학생 페이지 필터링을 위한 단 하나의 최종 쿼리 메소드
	@Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorSimpleDto(ci.employee.user.userId, ci.employee.emplNm) " +
	           "FROM CnslrInfo ci " +
	           "WHERE ci.isActive = true " +
	           "AND (:specialty = 'all' OR ci.cnslSpec = :specialty) " +
	           "AND (:counselorId = 'all' OR ci.employee.user.userId = :counselorId)")
	List<CounselorSimpleDto> findSimpleActiveCounselorsByFilter(@Param("specialty") String specialty, @Param("counselorId") String counselorId);

    // '전체'를 선택했을 때 사용할, 모든 활성 상담사를 조회하는 메소드
    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorSimpleDto(ci.employee.user.userId, ci.employee.emplNm) " +
           "FROM CnslrInfo ci WHERE ci.isActive = true")
    List<CounselorSimpleDto> findAllSimpleActiveCounselors();
    
    // 학생 페이지 상담사 필터링을 위한 JPQL 쿼리
    @Query("SELECT new kr.ac.dhuniv.counsel.dto.CounselorSimpleDto(ci.employee.user.userId, ci.employee.emplNm) " +
           "FROM CnslrInfo ci " +
           "WHERE ci.isActive = true " +
           "AND (:specialty = 'all' OR ci.cnslSpec = :specialty)")
    List<CounselorSimpleDto> findSimpleActiveCounselorsBySpecialty(@Param("specialty") String specialty);

    @Query(value =
        "SELECT " +
        "   e.empl_no as counselorId, e.empl_nm as name, e.empl_eml_addr as email, e.empl_telno as phone, " +
        "   ci.cnsl_spec as specialty, CASE WHEN ci.is_active = true THEN 'active' ELSE 'inactive' END as status, ci.intro as intro, " +
        "   (SELECT COUNT(*) FROM cnsl_aply ca WHERE ca.empl_no = e.empl_no) as consultationCount, " +
        "   COALESCE((SELECT AVG(rs.satisf_score) FROM cnsl_rslt rs JOIN cnsl_aply ca ON rs.cnsl_aply_id = ca.cnsl_aply_id WHERE ca.empl_no = e.empl_no), 0.0) as averageRating " +
        "FROM cnslr_info ci " +
        "JOIN empl_info e ON ci.empl_no = e.empl_no " +
        "WHERE e.empl_no = :userId",
        nativeQuery = true)
    Optional<Object[]> findCounselorDetailByUserIdNative(@Param("userId") String userId);
    
    Optional<CnslrInfo> findByEmployee_User_UserId(String userId);

    // 관리자 페이지 목록/필터 조회를 위한 네이티브 쿼리
    @Query(value =
            "SELECT " +
            "   e.empl_no as counselorId, " +
            "   e.empl_nm as name, " +
            "   e.empl_eml_addr as email, " +
            "   e.empl_telno as phone, " +
            "   ci.cnsl_spec as specialty, " +
            "   CASE WHEN ci.is_active = true THEN 'active' ELSE 'inactive' END as status, " +
            "   ci.intro as intro, " +
            "   (SELECT COUNT(*) FROM cnsl_aply ca WHERE ca.empl_no = e.empl_no) as consultationCount, " +
            "   COALESCE((SELECT AVG(rs.satisf_score) FROM cnsl_rslt rs JOIN cnsl_aply ca ON rs.cnsl_aply_id = ca.cnsl_aply_id WHERE ca.empl_no = e.empl_no), 0.0) as averageRating " +
            "FROM cnslr_info ci " +
            "JOIN empl_info e ON ci.empl_no = e.empl_no " +
            "WHERE ci.is_active = true " +
            "AND (:specialty = 'all' OR ci.cnsl_spec = :specialty) " +
            "AND (:counselorId = 'all' OR ci.empl_no = :counselorId) ",
            nativeQuery = true)
        List<Object[]> findActiveCounselorsByFilterNative(@Param("specialty") String specialty, @Param("counselorId") String counselorId);

}

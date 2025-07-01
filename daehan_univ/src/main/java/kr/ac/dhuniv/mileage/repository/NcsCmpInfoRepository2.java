package kr.ac.dhuniv.mileage.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.mileage.dto.employee.CompletedProgramDTO;
import kr.ac.dhuniv.mileage.dto.employee.CompletedStudentDTO;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;

@Repository
public interface NcsCmpInfoRepository2 extends JpaRepository<NcsCmpInfo, Long> {

    // 특정 프로그램 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByProgram_PrgId(Long prgId);

    // 특정 학생 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByStudent_StdId(Long stdId);
    
    // 이수완료 되었지만 마일리지가 지급되지 않은 프로그램 목록 조회
    @Query("""
    	    SELECT new kr.ac.dhuniv.mileage.dto.employee.CompletedProgramDTO(
    	        p.prgId,
    	        p.prgNm,
    	        c.cciNm,
    	        p.prgStartDate,
    	        p.prgEndDate,
    	        cInfo.completeDate
    	    )
    	    FROM NcsCmpInfo cInfo
    	    JOIN cInfo.program p
    	    JOIN p.coreCpt c
    	    WHERE cInfo.statusCode = 'CMP'
    	    AND cInfo.cmpId NOT IN (
    	        SELECT h.completion.cmpId FROM StdMileageHist h
    	    )
    	    GROUP BY p.prgId, p.prgNm, c.cciNm, p.prgStartDate, p.prgEndDate, cInfo.completeDate
    	""")
    List<CompletedProgramDTO> findCompletedButUnpaidPrograms();
    
    // 해당 프로그램 이수자 중 마일리지 미지급자 목록 조회
    /*
     * LEFT JOIN StdMileageHist h ON h.completion = cInfo -> LEFT JOIN : StdMileageHist 마일리지 이력이 없는 리스트가 출력되어야 하므로...
     */
    @Query("""
        SELECT new kr.ac.dhuniv.mileage.dto.employee.CompletedStudentDTO(
            s.stdId,
            p.prgId,
            cInfo.cmpId,
            u.userId,
            s.stdNm,
            p.prgNm,
            c.cciNm,
            cInfo.completeDate,
            CASE WHEN h.mlgId IS NOT NULL THEN '지급완료' ELSE '미지급' END,
            m.mileageScore
        )
        FROM NcsCmpInfo cInfo
        JOIN cInfo.student s
        JOIN s.user u
        JOIN cInfo.program p
        JOIN p.coreCpt c
        JOIN NcsPrgMileage m ON m.program = p
        LEFT JOIN StdMileageHist h ON h.completion = cInfo
        WHERE (:programId IS NULL OR p.prgId = :programId)
          AND (cInfo.completeDate = COALESCE(:completeDate, cInfo.completeDate))
    """)
    List<CompletedStudentDTO> findCompletedStudents(@Param("programId") Long ProgramId, @Param("completeDate") LocalDateTime completeDate);

    // 전체 프로그램 이수자 중 마일리지 미지급자 목록 조회 (검색 적용)
    @Query("""
        SELECT new kr.ac.dhuniv.mileage.dto.employee.CompletedStudentDTO(
            s.stdId,
            p.prgId,
            a.cmpId,
            u.userId,
            s.stdNm,
            p.prgNm,
            c.cciNm,
            a.completeDate,
            CASE WHEN h.mlgId IS NOT NULL THEN '지급완료' ELSE '미지급' END,
            m.mileageScore
        )
        FROM NcsCmpInfo a
        JOIN a.student s
        JOIN s.user u
        JOIN a.program p
        JOIN p.coreCpt c
        JOIN NcsPrgMileage m ON m.program = p
        LEFT JOIN StdMileageHist h ON h.completion = a
        WHERE p.prgStartDate >= COALESCE(:startDate, p.prgStartDate)
          AND p.prgEndDate   <= COALESCE(:endDate,   p.prgEndDate)
          AND (:competencyId IS NULL OR c.cciId = :competencyId)
          AND (:programId    IS NULL OR p.prgId = :programId)
          AND h.mlgId        IS NULL
    """)
    List<CompletedStudentDTO> findCompletedStudentsByFilter(
        @Param("startDate")       LocalDateTime startDate,
        @Param("endDate")         LocalDateTime endDate,
        @Param("competencyId")    Long competencyId,
        @Param("programId")       Long programId
    );

    @Query("SELECT a FROM NcsCmpInfo a WHERE a.student.stdId = :stdId AND a.program.prgId = :prgId")
    Optional<NcsCmpInfo> findByStudentAndProgramId(
        @Param("stdId") Long stdId,
        @Param("prgId") Long prgId
    );

}

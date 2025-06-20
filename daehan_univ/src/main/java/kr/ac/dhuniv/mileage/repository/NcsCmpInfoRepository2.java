package kr.ac.dhuniv.mileage.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.mileage.dto.CompletedStudentDto;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;

@Repository
public interface NcsCmpInfoRepository2 extends JpaRepository<NcsCmpInfo, Long> {

    // 특정 프로그램 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByProgram_PrgId(Long prgId);
    
    // 특정 학생 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByStudent_StdId(Long stdId);
    
    /*
     * StdInfo 엔티티가 `private User user`를 사용하도록 변경됨에 따라,
     * 쿼리 내부의 `s.stdNo` 경로가 더 이상 유효하지 않아 에러를 발생시키므로 임시 주석 처리합니다.
     * 추후 마일리지 파트 담당자가 `s.user.userId`로 수정해야 합니다.
     */
    /*
    @Query("""
        SELECT new kr.ac.dhuniv.mileage.dto.CompletedStudentDto(
            s.stdId,
            p.prgId,
            a.cmpId,
            s.stdNo,
            s.stdNm,
            p.prgNm,
            c.cciNm,
            a.completeDate,
            CASE WHEN h.mlgId IS NOT NULL THEN '지급완료' ELSE '미지급' END,
            m.mileageScore
        )
        FROM NcsCmpInfo a
        JOIN a.student s
        JOIN a.program p
        JOIN p.coreCpt c
        JOIN NcsPrgMileage m ON m.program = p
        LEFT JOIN StdMileageHist h ON h.completion = a
        WHERE p.prgStartDate >= COALESCE(:startDate, p.prgStartDate)
          AND p.prgEndDate <= COALESCE(:endDate, p.prgEndDate)
          AND (:coreCptId IS NULL OR c.cciId = :coreCptId)
          AND (:programId IS NULL OR p.prgId = :programId)
          AND h.mlgId IS NULL
    """)
    List<CompletedStudentDto> findCompletedStudentsByFilter(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("coreCptId") Long coreCptId,
        @Param("programId") Long programId
    );
    */

    /*
     * 위와 동일한 이유(`a.student.stdNo` 경로 문제)로 주석 처리합니다.
     */
    /*
    @Query("SELECT a FROM NcsCmpInfo a WHERE a.student.stdNo = :stdId AND a.program.prgNm = :prgId")
    Optional<NcsCmpInfo> findByStudentAndProgramId(@Param("stdId") Long stdId, @Param("prgId") Long prgId);
    */
    
}
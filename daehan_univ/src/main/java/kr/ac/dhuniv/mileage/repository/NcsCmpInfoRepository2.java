package kr.ac.dhuniv.mileage.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    
    // 프로그램 이수자 중 마일리지 미지급자 목록 조회(전체목록 또는 검색목록)
    /* 
     * LEFT JOIN : 기준 테이블(NcsCmpInfo)의 모든 데이터가 나오고, 조인 대상(StdMileageHist)이 없으면 NULL 처리됨
     * CASE WHEN h.mlgId IS NOT NULL THEN '지급완료' ELSE '미지급' END : 해당 이수건에 대해 h.mlgId가 존재한다면 마일리지가 지급됨을 의미하므로 '지급완료'
     * 
     * 검색조건 : 운영기간 startDate ~ endDate, 핵심역량 coreCptId, 비교과 프로그램 programId
     * COALESCE(:startDate, p.prgStartDate)
     *  - :startDate가 입력값으로 존재하면 → 그 값을 사용
     *  - :startDate가 null이면 → p.prgStartDate 자신의 값을 사용
     * programId IS NULL OR p.prgId = :programId 의
     *  - 사용자가 programId를 선택 안 했으면 모든 프로그램 포함 (IS NULL) -> 전체 이수자 목록
     *  - 사용자가 programId를 선택했으면 해당 ID만 필터 (= :programId)  -> 검색조건 적용된 이수자 목록
     * h.mlgId IS NULL -> '미지급'만 조회
     */
    @Query("""
    	    SELECT new kr.ac.dhuniv.mileage.dto.CompletedStudentDto(
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

}



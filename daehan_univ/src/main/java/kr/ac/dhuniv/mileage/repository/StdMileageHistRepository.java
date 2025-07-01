package kr.ac.dhuniv.mileage.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.dhuniv.mileage.domain.StdMileageHist;
import kr.ac.dhuniv.mileage.dto.employee.PaymentHistoryDTO;
import kr.ac.dhuniv.mileage.dto.employee.ProgramTopDTO;
import kr.ac.dhuniv.mileage.dto.employee.RecentActivityDTO;
import kr.ac.dhuniv.mileage.dto.student.MileageHistoryDTO;

public interface StdMileageHistRepository extends JpaRepository<StdMileageHist, Long> {
	
    //마일리지 획득 또는 사용 합계 (additionCode: 'plus'는 획득, 'minus'는 사용)
	@Query("SELECT COALESCE(SUM(h.mileageScore), 0) " +
		       "FROM StdMileageHist h " +
		       "WHERE h.studentTotal.student.user.userId = :userId " +
		       "AND h.additionCode = :additionCode ")
	BigDecimal sumMileageByStudentId(@Param("userId") String userId, @Param("additionCode") String additionCode);
	
	//마일리지 참여 프로그램 수
	@Query("SELECT COUNT(DISTINCT h.programMileage.prgMlgCode) " +
		       "FROM StdMileageHist h " +
		       "WHERE h.studentTotal.student.user.userId = :userId")
	int countDistinctPrograms(@Param("userId") String userId);

	
	//특정기간 마일리지 획득 또는 사용 합계 (additionCode: 'plus'는 획득, 'minus'는 사용)
	@Query("SELECT COALESCE(SUM(h.mileageScore), 0) " +
		       "FROM StdMileageHist h " +
		       "WHERE h.studentTotal.student.user.userId = :userId " +
		       "AND h.additionCode = :additionCode " +
		       "AND h.mileageDate BETWEEN :periodStart AND :periodEnd")
	BigDecimal sumMileageInPeriod(
	    @Param("userId") String userId,
	    @Param("periodStart") LocalDateTime periodStart,
	    @Param("periodEnd") LocalDateTime periodEnd,
	    @Param("additionCode") String additionCode);
	
	//특정 학생의 핵심역량별 마일리지 합계
    @Query("SELECT h.programMileage.program.coreCpt.cciNm, SUM(h.mileageScore) " +
            "FROM StdMileageHist h " +
            "WHERE h.studentTotal.student.user.userId = :userId " +
            "GROUP BY h.programMileage.program.coreCpt.cciNm")
    List<Object[]> sumCompetencyMileageGrouped(@Param("userId") String userId);
    
    default Map<String, BigDecimal> sumByCoreCompetency(String userId) {
        List<Object[]> result = sumCompetencyMileageGrouped(userId);
        return result.stream()
            .collect(Collectors.toMap(
                r -> (String) r[0],
                r -> (BigDecimal) r[1]
            ));
    }
    
    // 특정학생의 마일리지 지급내역 추출 (검색조건 및 페이징 적용)
    @Query("""
    	    SELECT new kr.ac.dhuniv.mileage.dto.student.MileageHistoryDTO(
    	        h.mileageDate,
    	        p.prgNm,
    	        p.prgStartDate,
    	        p.prgEndDate,
    	        p.coreCpt.cciNm,
    	        h.mileageScore
    	    )
    	    FROM StdMileageHist h
    	    JOIN h.completion c
    	    JOIN c.program p
    	    WHERE h.studentTotal.student.user.userId = :userId
    	        AND h.mileageDate >= COALESCE(:startDate, h.mileageDate)
    		    AND h.mileageDate <= COALESCE(:endDate, h.mileageDate)
    		    AND (:competencyId IS NULL OR p.coreCpt.cciId = :competencyId)
    		ORDER BY 
		        CASE WHEN :sort = 'latest' THEN h.mileageDate END DESC,
		        CASE WHEN :sort = 'oldest' THEN h.mileageDate END ASC,
		        CASE WHEN :sort = 'points-high' THEN h.mileageScore END DESC,
		        CASE WHEN :sort = 'points-low' THEN h.mileageScore END ASC
    	    """)
    	Page<MileageHistoryDTO> findFiltered(
    	    @Param("userId") String userId,
    	    @Param("startDate") LocalDateTime startDate,
    	    @Param("endDate") LocalDateTime endDate,
    	    @Param("competencyId") Long competencyId,
    	    @Param("sort") String sort,
    	    Pageable pageable
    	);
    
	//학생 마일리지 점수 이력 테이블에 해당 이수 ID(마일리지 지급 이력) 유무 
    boolean existsByCompletion_CmpId(Long cmpId);
    
    // [교직원 포털 마일리지 대시보드]
    // 해당 기간 내 총 지급 마일리지 (지급취소는 제외)
    @Query("""
    		SELECT COALESCE(SUM(h.mileageScore), 0)
    		FROM StdMileageHist h
    		WHERE h.mileageDate BETWEEN :start AND :end
    		  AND h.mlgStatCode = 'COM'
    		""")
    long sumMileageByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 상위 5개 프로그램 (지급된 마일리지합계 기준 내림차순, 총 지급 마일리지에서 지급취소는 제외)
    @Query("""
        SELECT new kr.ac.dhuniv.mileage.dto.employee.ProgramTopDTO(
            h.programMileage.program.prgNm,
            h.programMileage.program.coreCpt.cciNm,
            COUNT(DISTINCT h.studentTotal.student.user.userId),
            SUM(h.mileageScore)
        )
        FROM StdMileageHist h
        WHERE h.mileageDate BETWEEN :start AND :end
          AND h.mlgStatCode = 'COM'
        GROUP BY h.programMileage.program.prgNm, h.programMileage.program.coreCpt.cciNm
        ORDER BY SUM(h.mileageScore) DESC
    """)
    List<ProgramTopDTO> findTop5Programs(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    // 최근 활동 내역 (일괄지급/개별지급 판단 포함, 지급한 총 마일리지에서 지급취소는 제외)
    /* 
     * COUNT(DISTINCT h.programMileage.program.prgId) -> Long 타입 반환
     * MIN(h.programMileage.program.prgNm) -> 사전순 첫번째 값
     * MAX(h.mileageDate) -> 동일한 mlgCode인데도 저장일시가 초이하 단위로 다르게 저장되어 마지막 저장된 일자를 선택함
     */
    @Query("""
        SELECT new kr.ac.dhuniv.mileage.dto.employee.RecentActivityDTO(
            MAX(h.mileageDate),
            CASE WHEN COUNT(h) > 1 THEN '일괄지급' ELSE '개별지급' END,
            COUNT(DISTINCT h.programMileage.program.prgId),
            COUNT(DISTINCT h.studentTotal.student.user.userId),
            MIN(h.programMileage.program.prgNm),
            MIN(h.studentTotal.student.stdNm),
            SUM(h.mileageScore)
        )
        FROM StdMileageHist h
        WHERE h.mlgStatCode = 'COM'
        GROUP BY h.mlgCode
        ORDER BY MAX(h.mileageDate) DESC
    """)
    List<RecentActivityDTO> findRecentActivities(Pageable pageable);
    
    //전체 마일리지 지급 내역 (검색 및 페이징 적용)
    @Query("""
    		SELECT new kr.ac.dhuniv.mileage.dto.employee.PaymentHistoryDTO(
    		    h.mlgId, 
    		    h.mileageDate, 
    		    s.user.userId, 
    		    s.stdNm, 
    		    p.program.coreCpt.cciNm, 
    		    p.program.prgNm, 
    		    h.mileageScore, 
    		    h.mlgStatCode
    		) 
            FROM StdMileageHist h 
            JOIN h.studentTotal st 
            JOIN st.student s 
            JOIN h.programMileage p 
            WHERE h.mileageDate >= COALESCE(:startDate, h.mileageDate)
    		  AND h.mileageDate <= COALESCE(:endDate, h.mileageDate)
              AND (:competencyId IS NULL OR p.program.coreCpt.cciId = :competencyId)
              AND (:programId IS NULL OR p.program.prgId = :programId)
              AND (:mlgStatCode IS NULL OR h.mlgStatCode = :mlgStatCode)
            ORDER BY h.mlgId DESC
           """)
   Page<PaymentHistoryDTO> findHistory(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("competencyId") Long competencyId,
                                       @Param("programId") Long programId,
                                       @Param("mlgStatCode") String mlgStatCode,
                                       Pageable pageable);
    
    //마일리지 단건 취소 
//    @Modifying
//    @Query("UPDATE StdMileageHist h SET h.mlgStatCode = 'CAN' WHERE h.mlgId = :id")
//    void cancelById(@Param("id") Long id);

    //마일리지 복수 취소
//    @Modifying
//    @Query("UPDATE StdMileageHist h SET h.mlgStatCode = 'CAN' WHERE h.mlgId IN :ids")
//    void cancelByIds(@Param("ids") List<Long> ids);
    
    // 단건 조회 - 학생 마일리지 총합까지 fetch
    @Query("SELECT h FROM StdMileageHist h JOIN FETCH h.studentTotal WHERE h.mlgId = :id")
    Optional<StdMileageHist> findWithStudentTotalById(@Param("id") Long id);

    // 복수 조회 - 학생 마일리지 총합까지 fetch
    @Query("SELECT h FROM StdMileageHist h JOIN FETCH h.studentTotal WHERE h.mlgId IN :ids")
    List<StdMileageHist> findAllWithStudentTotalByIds(@Param("ids") List<Long> ids);

    //mlgCode의 숫자부분 중에서 가장 큰수 선택
    @Query("SELECT MAX(CAST(SUBSTRING(m.mlgCode, 4) AS int)) FROM StdMileageHist m")
    Integer findMaxMlgCodeNumber();
    
}

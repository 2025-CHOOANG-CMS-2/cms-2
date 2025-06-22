package kr.ac.dhuniv.mileage.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.dhuniv.mileage.domain.StdMileageHist;

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
	
	//
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

	//학생 마일리지 점수 이력 테이블에 해당 이수 ID(마일리지 지급 이력) 유무 
    boolean existsByCompletion_CmpId(Long cmpId);

    //mlgId의 숫자부분 중에서 가장 큰수 선택
    @Query("SELECT MAX(CAST(SUBSTRING(m.mlgCode, 4) AS int)) FROM StdMileageHist m")
    Integer findMaxMlgCodeNumber();
}

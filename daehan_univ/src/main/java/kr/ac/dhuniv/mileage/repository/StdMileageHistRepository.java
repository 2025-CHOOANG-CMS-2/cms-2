//package kr.ac.dhuniv.mileage.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import kr.ac.dhuniv.mileage.domain.StdMileageHist;
//
//public interface StdMileageHistRepository extends JpaRepository<StdMileageHist, Long> {
//	
//	//학생 마일리지 점수 이력 테이블에 해당 이수 ID(마일리지 지급 이력) 유무 
//    boolean existsByCompletion_CmpId(Long cmpId);
//
//    //mlgId의 숫자부분 중에서 가장 큰수 선택
//    @Query("SELECT MAX(CAST(SUBSTRING(m.mlgCode, 4) AS int)) FROM StdMileageHist m")
//    Integer findMaxMlgCodeNumber();
//}

<<<<<<< HEAD
package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnlrSchd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository // @Repository 추가
public interface CnlrSchdRepository extends JpaRepository<CnlrSchd, Long> {

    // [수정] 모든 메소드 이름의 경로를 EmplInfo의 새로운 구조에 맞게 변경
    List<CnlrSchd> findByEmployee_User_UserIdAndDayCodeBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    Optional<CnlrSchd> findByEmployee_User_UserIdAndDayCode(String userId, LocalDate date);
    
    List<CnlrSchd> findByEmployee_User_UserIdInAndDayCodeBetween(List<String> userIds, LocalDate startDate, LocalDate endDate);
    
    @Modifying
    void deleteByEmployee_User_UserId(String userId);
}
=======
//package kr.ac.dhuniv.counsel.repository;
//
//import kr.ac.dhuniv.counsel.domain.CnlrSchd;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//public interface CnlrSchdRepository extends JpaRepository<CnlrSchd, Long> {
//
//    // 특정 상담사의 특정 기간 동안의 예외 일정을 조회
//    List<CnlrSchd> findByEmployee_EmplNoAndDayCodeBetween(String emplNo, LocalDate startDate, LocalDate endDate);
//    
//    // 특정 상담사의 특정 날짜 예외 일정을 조회
//    Optional<CnlrSchd> findByEmployee_EmplNoAndDayCode(String emplNo, LocalDate date);
//    
//    // 여러 상담사들의 특정 기간 예외 일정을 한 번에 조회합니다.
//    List<CnlrSchd> findByEmployee_EmplNoInAndDayCodeBetween(List<String> emplNos, LocalDate startDate, LocalDate endDate);
//    
//    // empl_no를 기준으로 모든 스케줄을 삭제합니다.
//    @Modifying // SELECT가 아닌 DELETE, UPDATE 쿼리일 때 필요
//    void deleteByEmployee_EmplNo(String emplNo);
//}
>>>>>>> 719e435733ab23b1c0e98395ed16698c4562da7f

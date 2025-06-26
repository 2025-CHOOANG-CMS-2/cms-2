package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnlrSchd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CnlrSchdRepository extends JpaRepository<CnlrSchd, Long> {

    // [수정] 올바른 엔티티 경로를 따르는 메소드 이름
    List<CnlrSchd> findByEmployee_User_UserIdInAndDayCodeBetween(List<String> userIds, LocalDate startDate, LocalDate endDate);
    
    @Modifying
    void deleteByEmployee_User_UserId(String userId);
    
    // [추가] 특정 상담사의 특정 기간 예외 스케줄을 조회하는 메소드
    List<CnlrSchd> findByEmployee_User_UserIdAndDayCodeBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    // [추가] 특정 상담사의 특정 날짜 예외 스케줄을 조회하는 메소드
    Optional<CnlrSchd> findByEmployee_User_UserIdAndDayCode(String userId, LocalDate date);
}
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
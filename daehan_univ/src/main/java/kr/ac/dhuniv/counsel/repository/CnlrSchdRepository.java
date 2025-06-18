package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnlrSchd;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CnlrSchdRepository extends JpaRepository<CnlrSchd, Long> {

    // 특정 상담사의 특정 기간 동안의 예외 일정을 조회
    List<CnlrSchd> findByEmployee_EmplNoAndDayCodeBetween(String emplNo, LocalDate startDate, LocalDate endDate);
    
    // 특정 상담사의 특정 날짜 예외 일정을 조회
    Optional<CnlrSchd> findByEmployee_EmplNoAndDayCode(String emplNo, LocalDate date);
}
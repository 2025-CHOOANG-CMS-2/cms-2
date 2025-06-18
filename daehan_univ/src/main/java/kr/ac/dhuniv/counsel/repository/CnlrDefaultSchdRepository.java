package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnlrDefaultSchd;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CnlrDefaultSchdRepository extends JpaRepository<CnlrDefaultSchd, Long> {

    // 특정 상담사의 주간 반복 근무 시간 전체를 조회
    List<CnlrDefaultSchd> findByEmployee_EmplNo(String emplNo);
}
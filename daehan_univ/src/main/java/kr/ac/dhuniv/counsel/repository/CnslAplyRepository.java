package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnslAply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CnslAplyRepository extends JpaRepository<CnslAply, Long> {

    // 여러 상담사들의 특정 기간 예약 내역을 한 번에 조회합니다.
    List<CnslAply> findByEmployee_User_UserIdInAndApplyDateTimeBetween(List<String> userIds, LocalDateTime startDateTime, LocalDateTime endDateTime);
    
}
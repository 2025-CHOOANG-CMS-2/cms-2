package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnslAply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface CnslAplyRepository extends JpaRepository<CnslAply, Long> {

    // [수정] 올바른 엔티티 경로를 따르는 메소드 이름
    List<CnslAply> findByEmployee_User_UserIdInAndApplyDateTimeBetween(List<String> userIds, LocalDateTime startDateTime, LocalDateTime endDateTime);

    // [추가] 특정 상담사에게 특정 시간에 예약이 있는지 확인하는 메소드
    boolean existsByEmployee_User_UserIdAndApplyDateTime(String userId, LocalDateTime applyDateTime);
    
    List<CnslAply> findByEmployee_User_UserIdInAndApplyDateTimeBetweenAndStatusCodeIn(
            List<String> userIds, 
            LocalDateTime startDateTime, 
            LocalDateTime endDateTime, 
            Collection<String> statusCodes
        );
}
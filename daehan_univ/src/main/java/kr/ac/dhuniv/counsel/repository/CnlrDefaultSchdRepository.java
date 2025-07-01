package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnlrDefaultSchd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CnlrDefaultSchdRepository extends JpaRepository<CnlrDefaultSchd, Long> {

    // [수정] 올바른 엔티티 경로를 따르는 메소드 이름
    List<CnlrDefaultSchd> findByEmployee_User_UserIdIn(List<String> userIds);
    
    @Modifying
    void deleteByEmployee_User_UserId(String userId);
    
    // [추가] 특정 상담사의 모든 기본 스케줄을 조회하는 메소드
    List<CnlrDefaultSchd> findByEmployee_User_UserId(String userId);
}
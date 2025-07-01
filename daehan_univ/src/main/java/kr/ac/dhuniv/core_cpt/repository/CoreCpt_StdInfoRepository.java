package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.std_info.domain.StdInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * 학생 정보를 조회하기 위한 레포지토리
 * — 학번(user.userId)으로 StdInfo를 가져오는 메서드를 정의
 */
public interface CoreCpt_StdInfoRepository extends JpaRepository<StdInfo, Long> {
    /**
     * @param userId User.userId 컬럼값(학번)
     * @return 해당 학번을 가진 StdInfo 엔티티(Optional)
     */
    Optional<StdInfo> findByUser_UserId(String userId);
    /**
     * ✅ 학생 학번(user_id) 기준으로 StdInfo 조회
     * @param userUserId 학번 or 사용자ID
     * @return StdInfo
     */
    Optional<StdInfo> findByUserUserId(String userUserId);
}
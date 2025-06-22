package kr.ac.dhuniv.mypage.repository;

import kr.ac.dhuniv.std_info.domain.StdInfo;
import kr.ac.dhuniv.user.User; // User 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StdMypageRepository extends JpaRepository<StdInfo, Long> {

    // ⭐ 중요: StdInfo 엔티티에 직접 stdNo 필드가 없으므로 이 메서드를 제거합니다. ⭐
    // Optional<StdInfo> findByStdNo(String stdNo); // 이 라인을 제거합니다.

    // ⭐ User 객체를 사용하여 StdInfo를 조회하는 메서드는 유지합니다. ⭐
    Optional<StdInfo> findByUser(User user);

    // 이메일 중복 체크를 위한 메서드는 유지합니다.
    Optional<StdInfo> findByStdEmlAddr(String stdEmlAddr);
}

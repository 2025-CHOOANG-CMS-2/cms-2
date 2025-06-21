package kr.ac.dhuniv.mypage.repository;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpMypageRepository extends JpaRepository<EmplInfo, Long> {

    /**
     * User 엔티티를 기준으로 교직원 정보를 조회합니다.
     * @param user 조회할 User 엔티티
     * @return 해당 User와 연관된 EmplInfo (존재하지 않을 수 있으므로 Optional)
     */
    Optional<EmplInfo> findByUser(User user);

    /**
     * 이메일 주소를 기준으로 교직원 정보를 조회합니다. (이메일 중복 체크용)
     * @param emplEmailAddr 조회할 이메일 주소
     * @return 해당 이메일 주소를 가진 EmplInfo (존재하지 않을 수 있으므로 Optional)
     */
    Optional<EmplInfo> findByEmplEmailAddr(String emplEmailAddr);
}

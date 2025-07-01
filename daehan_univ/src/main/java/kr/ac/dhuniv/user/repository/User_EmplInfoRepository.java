package kr.ac.dhuniv.user.repository;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface User_EmplInfoRepository extends JpaRepository<EmplInfo,Long> {

        Optional<EmplInfo> findByUser_UserId(String userId); // 사번으로 조회

}

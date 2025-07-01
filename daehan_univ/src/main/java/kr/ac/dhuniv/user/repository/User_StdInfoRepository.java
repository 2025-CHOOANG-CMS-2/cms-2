package kr.ac.dhuniv.user.repository;

import kr.ac.dhuniv.std_info.domain.StdInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface User_StdInfoRepository extends JpaRepository<StdInfo, Long> {
    Optional<StdInfo> findByUser_UserId(String userId);
}

package kr.ac.dhuniv.mileage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.std_info.domain.StdInfo;

@Repository
public interface StdInfoRepository2  extends JpaRepository<StdInfo, Long> {
    Optional<StdInfo> findByStdId(Long stdId);
}

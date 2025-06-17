package kr.ac.dhuniv.mileage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;

public interface NcsPrgMileageRepository2 extends JpaRepository<NcsPrgMileage, Long> {
    
    // 특정 프로그램 ID에 할당된 마일리지 점수 (아직 마일리지가 할당되지 않은 경우도 있음 : Optional
    Optional<NcsPrgMileage> findByProgram_PrgId(Long prgId);
}

package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreCpt_MileageRepository extends JpaRepository<NcsPrgMileage,Long> {

    /**
     * 프로그램 ID로 마일리지 정보를 조회
     *
     * @param program 비교과 프로그램 엔티티
     * @return 마일리지 정보
     */
    NcsPrgMileage findByProgram(NcsPrgInfo program);
}

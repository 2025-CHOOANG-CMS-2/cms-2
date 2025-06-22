package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoreCpt_NcsPrgInfoRepository  extends JpaRepository<NcsPrgInfo, Long> {
    /**
     * ✅ 핵심역량별 프로그램 조회
     */
    List<NcsPrgInfo> findByCoreCpt(CoreCptInfo coreCpt);
}

package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CoreCpt_NcsPrgInfoRepository  extends JpaRepository<NcsPrgInfo, Long> {
    /**
     * ✅ 핵심역량별 프로그램 조회
     */
    List<NcsPrgInfo> findByCoreCpt(CoreCptInfo coreCpt);

    /**
     * 해당 프로그램의 신청자 수를 집계하는 메서드
     *
     * @param prgId 비교과 프로그램 ID
     * @return 신청자 수
     */
    @Query("SELECT COUNT(a) FROM NcsPrgAply a WHERE a.program.prgId = :prgId")
    int countAppliedStudents(Long prgId);
}

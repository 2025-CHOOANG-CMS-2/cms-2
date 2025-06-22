package kr.ac.dhuniv.ncs.repository;

import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NcsPrgInfoRepository extends JpaRepository<NcsPrgInfo, Long> {

    /**
     * 프로그램 코드(PRG_CODE)로 단건 조회
     */
    Optional<NcsPrgInfo> findByPrgCode(String prgCode);

    /**
     * 프로그램명에 키워드 포함된 목록 조회 (페이징)
     */
    Page<NcsPrgInfo> findByPrgNmContainingIgnoreCase(String keyword, Pageable pageable);

    /**
     * 카테고리(CCI_ID)로 목록 조회 (페이징)
     */
    Page<NcsPrgInfo> findByCoreCpt_CciId(Long cciId, Pageable pageable);

}
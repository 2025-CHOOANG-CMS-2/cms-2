package kr.ac.dhuniv.mileage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;

public interface NcsCmpInfoRepository2 extends JpaRepository<NcsCmpInfo, Long> {

    // 특정 프로그램 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByProgram_PrgId(String prgId);
}

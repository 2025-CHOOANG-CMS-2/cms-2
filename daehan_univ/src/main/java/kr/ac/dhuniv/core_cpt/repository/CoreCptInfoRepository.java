package kr.ac.dhuniv.core_cpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;

import java.util.List;
import java.util.Optional;

public interface CoreCptInfoRepository extends JpaRepository<CoreCptInfo, Long>{

    // cciId가 특정 prefix로 시작하는 것 중 가장 큰 값을 가진 항목 조회 (최상위/하위 코드 생성용)
    Optional<CoreCptInfo> findTopByCciIdStartingWithOrderByCciIdDesc(String prefix);

    // 특정 cciId 값으로 엔티티 조회 (자기참조용)
    Optional<CoreCptInfo> findByCciId(String cciId);

    // 상위 역량 (parent == null) 목록 조회
    List<CoreCptInfo> findByParentIsNull();

}

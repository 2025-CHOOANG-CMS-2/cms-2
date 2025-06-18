package kr.ac.dhuniv.mileage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;

public interface CoreCptInfoRepository2 extends JpaRepository<CoreCptInfo, Long> {
	// parent가 null인 역량만 (최상위 핵심역량)
    List<CoreCptInfo> findByParentIsNull();
}

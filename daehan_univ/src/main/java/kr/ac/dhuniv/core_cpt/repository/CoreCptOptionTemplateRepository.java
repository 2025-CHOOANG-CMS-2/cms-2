package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoreCptOptionTemplateRepository extends JpaRepository<CoreCptOptionTemplate, Long> {
    // ord 기준 정렬된 선택지 반환
    List<CoreCptOptionTemplate> findAllByOrderByOrdAsc();
}

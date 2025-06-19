package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptEval;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ✅ CoreCptEvalRepository
 * - CoreCptEval 저장용 리포지토리
 */

public interface CoreCptEvalRepository extends JpaRepository<CoreCptEval, Long> {
}
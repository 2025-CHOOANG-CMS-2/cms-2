package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.counsel.domain.CnslRslt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CnslRsltRepository extends JpaRepository<CnslRslt, Long> {
    
	boolean existsByCounselingApplication_Id(Long applyId);

}
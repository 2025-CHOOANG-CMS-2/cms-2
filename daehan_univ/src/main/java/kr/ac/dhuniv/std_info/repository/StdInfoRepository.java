package kr.ac.dhuniv.std_info.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 
import kr.ac.dhuniv.std_info.domain.StdInfo;

public interface StdInfoRepository extends JpaRepository<StdInfo, Long> { 
	
	 @Query("SELECT MAX(CAST(SUBSTRING(s.stdNo, 4) AS int)) FROM StdInfo s WHERE s.stdNo LIKE :prefix%")
	    Integer findMaxStdNoNumber(@Param("prefix") String prefix);
	 
}
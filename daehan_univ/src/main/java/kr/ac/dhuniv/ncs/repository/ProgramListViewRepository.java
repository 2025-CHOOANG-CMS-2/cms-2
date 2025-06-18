package kr.ac.dhuniv.ncs.repository;

import kr.ac.dhuniv.ncs.domain.ProgramListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramListViewRepository extends JpaRepository<ProgramListView, Long> {
	 //Page<ProgramListView> findAll(Pageable pageable);
}

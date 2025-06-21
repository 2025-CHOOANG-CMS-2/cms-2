package kr.ac.dhuniv.counsel.repository;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmplInfoRepository extends JpaRepository<EmplInfo, Long> {

    @Query(value =
        "SELECT e.empl_no, e.empl_nm, e.empl_eml_addr, e.empl_telno " +
        "FROM public.empl_info e " +
        "JOIN public.user_info u ON e.empl_no = u.user_id " +
        "JOIN public.user_roles ur ON u.user_idx = ur.user_id " +
        "JOIN public.role_info ri ON ur.role_id = ri.id " +
        "WHERE ri.role_name IN ('COUNSELOR', 'PROFESSOR') " +
        "AND e.empl_no NOT IN (SELECT ci.empl_no FROM public.cnslr_info ci)",
        nativeQuery = true)
    List<Object[]> findUnregisteredCounselorsNative();

    Optional<EmplInfo> findByUser_UserId(String userId);
}
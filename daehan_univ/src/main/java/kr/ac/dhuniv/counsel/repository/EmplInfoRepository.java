package kr.ac.dhuniv.counsel.repository; // 경로는 실제 위치에 맞게 수정

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;

@Repository
public interface EmplInfoRepository extends JpaRepository<EmplInfo, Long> {

    // User 엔티티에 roles 필드를 추가할 수 없으므로, 이 쿼리는 네이티브 SQL을 유지합니다.
    @Query(value = 
        "SELECT DISTINCT e.empl_no, e.empl_nm, e.empl_eml_addr, e.empl_telno " +
        "FROM public.empl_info e " +
        "JOIN public.user_info u ON e.empl_no = u.user_id " +
        "JOIN public.user_roles ur ON u.user_idx = ur.user_id " +
        "JOIN public.role_info ri ON ur.role_id = ri.id " +
        "WHERE ri.role_name IN ('COUNSELOR', 'PROFESSOR') " +
        "AND e.empl_no NOT IN (SELECT ci.empl_no FROM public.cnslr_info ci)",
        nativeQuery = true)
    List<Object[]> findUnregisteredCounselorsNative();
    
    // 이 메소드는 user.userId를 참조하도록 변경해야 합니다.
    Optional<EmplInfo> findByUser_UserId(String userId);
}
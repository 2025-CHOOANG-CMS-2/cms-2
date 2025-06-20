//package kr.ac.dhuniv.counsel.repository;
//
//import kr.ac.dhuniv.counsel.domain.CnslAply;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Repository
//public interface CnslAplyRepository extends JpaRepository<CnslAply, Long> {
//
//    /**
//     * 특정 상담사의 특정 기간 동안의 모든 상담 신청 내역을 조회합니다.
//     * Spring Data JPA의 '쿼리 메소드' 기능에 의해 이름만으로 자동으로 쿼리가 생성됩니다.
//     * @param emplNo 상담사 사번
//     * @param startOfDay 조회를 시작할 날짜와 시간 (예: 2025-06-20 00:00:00)
//     * @param endOfDay 조회를 종료할 날짜와 시간 (예: 2025-06-20 23:59:59)
//     * @return 상담 신청 엔티티 리스트
//     */
//    List<CnslAply> findByEmployee_EmplNoAndApplyDateTimeBetween(String emplNo, LocalDateTime startOfDay, LocalDateTime endOfDay);
//    
//    // 여러 상담사들의 특정 기간 예약 내역을 한 번에 조회합니다.
//    List<CnslAply> findByEmployee_EmplNoInAndApplyDateTimeBetween(List<String> emplNos, LocalDateTime startDateTime, LocalDateTime endDateTime);
//}
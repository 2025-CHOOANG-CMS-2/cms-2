package kr.ac.dhuniv.admin.repository;

import java.util.Optional; // Optional 임포트 추가

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import kr.ac.dhuniv.std_info.domain.StdInfo;

// StdInfo 엔티티의 ID 타입이 Long이라는 가정하에 JpaRepository<StdInfo, Long>으로 선언되어 있습니다.
public interface AdminStdRepository extends JpaRepository<StdInfo, Long> {

    // 학번(stdNo)의 최대값을 찾는 쿼리 (기존 코드 유지)
    @Query("SELECT MAX(CAST(SUBSTRING(s.stdNo, 4) AS int)) FROM StdInfo s WHERE s.stdNo LIKE :prefix%")
    Integer findMaxStdNoNumber(@Param("prefix") String prefix);

    // 모든 학생 정보를 페이징하여 조회 (JpaRepository가 기본으로 제공)
    Page<StdInfo> findAll(Pageable pageable);

    // 이메일로 학생을 찾는 메서드 (기존 코드 유지)
    Optional<StdInfo> findByEmail(String email);

    // updateStudent와 deleteStudent 메서드에서 이 findByStdNo를 사용합니다.
    Optional<StdInfo> findByStdNo(String stdNo);
}
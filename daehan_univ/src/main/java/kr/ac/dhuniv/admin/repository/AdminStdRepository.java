package kr.ac.dhuniv.admin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import kr.ac.dhuniv.std_info.domain.StdInfo;

// StdInfo 엔티티의 ID 타입이 Long이므로 JpaRepository<StdInfo, Long>으로 선언합니다.
// JpaSpecificationExecutor<StdInfo>를 상속받아 Specification 기반의 동적 쿼리를 지원합니다.
public interface AdminStdRepository extends JpaRepository<StdInfo, Long>, JpaSpecificationExecutor<StdInfo> {

    // 이메일로 학생을 찾는 메서드 (기존 코드 유지)
    Optional<StdInfo> findByEmail(String email);

    // 전화번호로 학생을 찾는 메서드 (서비스에서 추가된 로직에 맞춰 필요)
    Optional<StdInfo> findByTel(String tel);

    // 학번(stdNo)으로 학생을 찾는 메서드 (PK가 아니더라도 유니크 컬럼으로 조회 가능)
    Optional<StdInfo> findByStdNo(String stdNo);

    /**
     * 특정 연도와 학과 코드에 해당하는 학번들 중 가장 큰 순번 부분을 찾아 반환합니다.
     * 학번 형식: `YYYY` (4자리 연도) + `NNN` (3자리 학과코드) + `MMM` (3자리 순번) = 총 10자리
     * 예: "2025011001"
     *
     * @param year 학번의 연도 부분 (예: "2025")
     * @param deptCode 학번의 학과 코드 부분 (예: "011")
     * @return 해당 연도와 학과 코드 조합 내에서 가장 큰 순번 (예: 1, 2, ...), 없으면 null
     */
    @Query("SELECT MAX(CAST(SUBSTRING(s.stdNo, 8, 3) AS int)) " + // 학번 8번째 문자부터 3자리가 순번 부분
           "FROM StdInfo s " +
           "WHERE SUBSTRING(s.stdNo, 1, 4) = :year " + // 학번 1번째부터 4자리가 연도 부분
           "AND SUBSTRING(s.stdNo, 5, 3) = :deptCode") // 학번 5번째부터 3자리가 학과 코드 부분
    Integer findMaxSequenceForStudentId(@Param("year") String year, @Param("deptCode") String deptCode);
}
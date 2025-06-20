//package kr.ac.dhuniv.admin.repository;
//
//import kr.ac.dhuniv.std_info.domain.StdInfo;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import java.util.Optional;
//
//public interface AdminStdRepository extends JpaRepository<StdInfo, Long>, JpaSpecificationExecutor<StdInfo> {
//
//    Optional<StdInfo> findByStdEmlAddr(String stdEmlAddr); // stdEmlAddr 필드에 대한 쿼리 메서드
//
//    Optional<StdInfo> findByStdTelno(String stdTelno);     // stdTelno 필드에 대한 쿼리 메서드
//
//    Optional<StdInfo> findByStdNo(String stdNo); // 학번(stdNo)으로 학생 정보 조회
//
//    /**
//     * 특정 연도와 학과 코드에 해당하는 학번들 중 가장 큰 순번 부분을 찾아 반환합니다.
//     * 학번 형식: `YYYY` (4자리 연도) + `DDD` (3자리 학과코드) + `SSS` (3자리 순번) = 총 10자리
//     * 예: "2025001001"
//     *
//     * @param year 학번의 연도 부분 (예: "2025")
//     * @param deptCode 학번의 학과 코드 부분 (예: "001")
//     * @return 해당 연도와 학과 코드 조합 내에서 가장 큰 순번 (예: 1, 2, ...), 없으면 null
//     */
//    @Query("SELECT MAX(CAST(SUBSTRING(s.stdNo, 8, 3) AS int)) " + // 8번째부터 3자리 (순번)
//           "FROM StdInfo s " +
//           "WHERE SUBSTRING(s.stdNo, 1, 4) = :year " + // 1번째부터 4자리 (연도)
//           "AND SUBSTRING(s.stdNo, 5, 3) = :deptCode") // 5번째부터 3자리 (학과코드)
//    Integer findMaxSequenceForStudentId(@Param("year") String year, @Param("deptCode") String deptCode);
//}

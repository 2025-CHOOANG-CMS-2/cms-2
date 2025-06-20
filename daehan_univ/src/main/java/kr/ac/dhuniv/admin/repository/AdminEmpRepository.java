package kr.ac.dhuniv.admin.repository;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminEmpRepository extends JpaRepository<EmplInfo, Long>, JpaSpecificationExecutor<EmplInfo> {

    // User 엔티티의 userId 필드를 통해 EmplInfo를 조회 (사번으로 교직원 정보 찾기)
    Optional<EmplInfo> findByUser_UserId(String userId);

    // 이메일 중복 체크를 위한 메서드
    Optional<EmplInfo> findByEmplEmailAddr(String emplEmailAddr);

    // 전화번호 중복 체크를 위한 메서드
    Optional<EmplInfo> findByEmplTelno(String emplTelno);

    // 새로운 사번 생성을 위한 쿼리 메서드:
    // empl_no가 이제 user.userId에 매핑되므로, user.userId 컬럼에서 연도(4자리)와 부서코드(3자리)를 추출하여
    // 해당 조건에 맞는 가장 큰 시퀀스 번호(마지막 3자리)를 찾습니다.
    @Query("SELECT MAX(CAST(SUBSTRING(e.user.userId, 8) AS INTEGER)) " +
           "FROM EmplInfo e " +
           "WHERE SUBSTRING(e.user.userId, 1, 4) = :year " +
           "AND SUBSTRING(e.user.userId, 5, 3) = :deptCd")
    Optional<Integer> findMaxSequenceForEmployeeId(@Param("year") String year, @Param("deptCd") String deptCd);
}

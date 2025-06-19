package kr.ac.dhuniv.admin.repository;

import kr.ac.dhuniv.std_info.domain.StdInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminStdRepository extends JpaRepository<StdInfo, Long>, JpaSpecificationExecutor<StdInfo> {

    // User 엔티티의 userId를 통해 StdInfo를 조회합니다.
    Optional<StdInfo> findByUser_UserId(String userId);

    // 이메일 중복 체크용
    Optional<StdInfo> findByStdEmlAddr(String stdEmlAddr);

    // 전화번호 중복 체크용
    Optional<StdInfo> findByStdTelno(String stdTelno);

    // 학번 생성을 위한 최대 시퀀스 조회
    @Query("SELECT MAX(CAST(SUBSTRING(s.user.userId, 7, 3) AS int)) " + // userId의 7번째 문자부터 3자리 (순번)
           "FROM StdInfo s " +
           "WHERE SUBSTRING(s.user.userId, 1, 4) = :year " + // userId의 1번째 문자부터 4자리 (년도)
           "AND SUBSTRING(s.user.userId, 5, 2) = :scsbjtCd") // userId의 5번째 문자부터 2자리 (학과 코드)
    Optional<Integer> findMaxSequenceForStudentId(@Param("year") String year, @Param("scsbjtCd") String scsbjtCd);
}

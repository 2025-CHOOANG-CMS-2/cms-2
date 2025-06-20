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
    // 학번 (user.userId)의 8번째 문자부터 3자리 (순번)을 추출하여 int로 캐스팅
    // 학번 (user.userId)의 1번째 문자부터 4자리 (년도)와 5번째 문자부터 3자리 (학과 코드)로 필터링
    // ⭐ SUBSTRING(s.user.userId, 5, 3)으로 변경하여 3자리 학과 코드를 추출 ⭐
    @Query("SELECT MAX(CAST(SUBSTRING(s.user.userId, 8, 3) AS int)) " + // 순번 시작 인덱스도 8로 변경 (YYYYDDDSSS)
           "FROM StdInfo s " +
           "WHERE SUBSTRING(s.user.userId, 1, 4) = :year " +
           "AND SUBSTRING(s.user.userId, 5, 3) = :scsbjtCd") // ⭐ 학과 코드 추출도 5번째부터 3자리로 변경 ⭐
    Optional<Integer> findMaxSequenceForStudentId(@Param("year") String year, @Param("scsbjtCd") String scsbjtCd);
}

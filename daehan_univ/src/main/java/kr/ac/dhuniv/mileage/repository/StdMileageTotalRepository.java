package kr.ac.dhuniv.mileage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.dhuniv.mileage.domain.StdMileageTotal;
import kr.ac.dhuniv.std_info.domain.StdInfo;

public interface StdMileageTotalRepository extends JpaRepository<StdMileageTotal, Long> {
	
	//해당학생(User테이블 userID 기준)의 마일리지 총점 정보
	@Query("SELECT t FROM StdMileageTotal t WHERE t.student.user.userId = :userId")
    Optional<StdMileageTotal> findByStudentUserId(@Param("userId") String userId);
	
	//총마일리지 점수로 내림차순 정렬
	List<StdMileageTotal> findAllByOrderByTotalMileageScoreDesc();
	
	//해당학생(StdInfo 테이블 기준)의 마일리지 총점 정보
	Optional<StdMileageTotal> findByStudent(StdInfo student);
	
	//재학생 기준 마일리지 보유 학생 수
	@Query("SELECT COUNT(t.student) FROM StdMileageTotal t WHERE t.student.statusCode = 'ENROLL' AND t.totalMileageScore > 0")
    long countActiveStudentsWithMileage();

	//재학생 기준 학생의 평균 마일리지
    @Query("SELECT COALESCE(AVG(t.totalMileageScore), 0) FROM StdMileageTotal t WHERE t.student.statusCode = 'ENROLL' AND t.totalMileageScore > 0")
    double avgMileagePerActiveStudent();
}

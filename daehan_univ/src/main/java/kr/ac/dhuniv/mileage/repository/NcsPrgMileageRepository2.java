package kr.ac.dhuniv.mileage.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;

public interface NcsPrgMileageRepository2 extends JpaRepository<NcsPrgMileage, Long> {
    
    // 특정 프로그램 ID에 할당된 마일리지 점수 (아직 마일리지가 할당되지 않은 경우도 있음 : Optional
    Optional<NcsPrgMileage> findByProgram_PrgId(Long prgId);
    
    // 비교과 프로그램 테이블 전체 컬럼 조회 (단, 핵심역량 테이블과 연계)
    // JOIN FETCH는 지연로딩을 방지하고 연관 엔티티를 한 번에 로딩
    @Query("SELECT m FROM NcsPrgMileage m " +
            "JOIN FETCH m.program p " +
            "JOIN FETCH p.coreCpt c ")
    List<NcsPrgMileage> findAllWithCoreCpt();
    
    // 이번 학기 활성 프로그램 수 (프로그램 기간과 이번 학기 기간이 겹치는 구간이 있으면 활성 프로그램으로 간주)
    @Query("SELECT COUNT(DISTINCT m.program.prgId) " +
            "FROM NcsPrgMileage m " +
            "WHERE m.program.prgStartDate <= :semesterEnd " +
            "  AND m.program.prgEndDate >= :semesterStart")
     long countActivePrograms(@Param("semesterStart") LocalDateTime semesterStart,
                              @Param("semesterEnd") LocalDateTime semesterEnd);
    
}

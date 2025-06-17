package kr.ac.dhuniv.mileage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kr.ac.dhuniv.mileage.dto.CompletedStudentDto;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;

@Repository
public interface NcsCmpInfoRepository2 extends JpaRepository<NcsCmpInfo, Long> {

    // 특정 프로그램 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByProgram_PrgId(Long prgId);
    
    // 특정 학생 ID로 이수자 목록 조회
    List<NcsCmpInfo> findByStudent_StdId(Long stdId);
    
    // 전체 이수자 목록 조회
    @Query("""
    	    SELECT new kr.ac.dhuniv.mileage.dto.CompletedStudentDto(
    	    s.stdNo,
    	    s.stdNm,
    	    p.prgNm,
    	    c.cciNm,
            a.completeDate,
            m.mileageScore
        )
        FROM NcsCmpInfo a 
        JOIN a.student s
        JOIN a.program p
        JOIN p.coreCpt c
        JOIN NcsPrgMileage m ON m.program = p
    """)
    List<CompletedStudentDto> findAllCompletedStudents();
}



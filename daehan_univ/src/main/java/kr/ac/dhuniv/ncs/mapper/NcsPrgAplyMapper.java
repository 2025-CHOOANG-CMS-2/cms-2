package kr.ac.dhuniv.ncs.mapper;

import kr.ac.dhuniv.ncs.dto.MyProgramDto;
import kr.ac.dhuniv.ncs.dto.NcsApplicationDto;
import kr.ac.dhuniv.ncs.dto.NcsPrgAplyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NcsPrgAplyMapper {

    /** 프로그램 현재 신청자 수 조회 */
    int countByProgramId(@Param("prgId") Long prgId);

    /** 특정 학생이 특정 프로그램에 신청했는지 확인 (중복 신청 방지용) */
    int existsByProgramIdAndStudentId(Map<String, Object> params);

    /** 신청 정보 등록 */
    int insert(NcsPrgAplyDto aplyDto);
    
    List<MyProgramDto> findMyProgramsByStudentId(@Param("stdId") Long stdId);
    
    /** 신청 목록 전체 조회 (JOIN) */
    List<NcsApplicationDto> findApplications(Map<String, Object> params);
    
    /** 신청 목록 전체 개수 조회 */
    int countApplications(Map<String, Object> params);

    /** 신청 단건 상세 조회 (JOIN) */
    NcsApplicationDto findApplicationById(@Param("aplyId") Long aplyId);

    /** 신청 상태 업데이트 (승인/반려) */
    int updateStatus(@Param("aplyId") Long aplyId, @Param("status") String status);
}
package kr.ac.dhuniv.counsel.mapper;

import kr.ac.dhuniv.counsel.dto.CounselingHistoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface CounselingHistoryMapper {

    /**
     * 학생의 상담 내역을 필터와 페이징 조건에 따라 조회합니다.
     * @param stdNo, startDate, status, type: 필터 조건
     * @param offset 건너뛸 데이터 수 (예: (page - 1) * 10)
     * @param pageSize 한 페이지에 보여줄 데이터 수 (예: 10)
     * @return 페이징 처리된 상담 내역 DTO 리스트
     */
    List<CounselingHistoryDto> findByStudentIdAndFilters(
        @Param("stdNo") String stdNo,
        @Param("startDate") LocalDateTime startDate,
        @Param("status") String status,
        @Param("type") String type,
        @Param("offset") int offset,
        @Param("pageSize") int pageSize
    );

    /**
     * 필터 조건에 맞는 전체 상담 내역의 개수를 조회합니다.
     * (전체 페이지 수를 계산하기 위해 필요)
     */
    long countByStudentIdAndFilters(
        @Param("stdNo") String stdNo,
        @Param("startDate") LocalDateTime startDate,
        @Param("status") String status,
        @Param("type") String type
    );
    
    Optional<CounselingHistoryDto> findByResultId(Long resultId);
}
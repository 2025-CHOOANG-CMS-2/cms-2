package kr.ac.dhuniv.counsel.mapper;

import kr.ac.dhuniv.counsel.dto.CounselingResultItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CounselorResultMapper {

    /**
     * 상담사 ID와 필터 조건에 맞는 상담 목록을 조회합니다. (페이징 적용)
     */
    List<CounselingResultItemDto> findByCounselorAndFilters(
            @Param("counselorId") String counselorId,
            @Param("statuses") List<String> statuses,
            @Param("period") String period,
            @Param("type") String type,
            @Param("offset") long offset,
            @Param("limit") int limit
    );

    /**
     * 상담사 ID와 필터 조건에 맞는 상담의 전체 개수를 조회합니다.
     */
    long countByCounselorAndFilters(
            @Param("counselorId") String counselorId,
            @Param("statuses") List<String> statuses,
            @Param("period") String period,
            @Param("type") String type
    );

    /**
     * [추가] '결과 수정' 시 필요한 상세 정보를 resultId로 조회합니다.
     */
    Optional<CounselingResultItemDto> findDetailByResultId(@Param("resultId") Long resultId);
}

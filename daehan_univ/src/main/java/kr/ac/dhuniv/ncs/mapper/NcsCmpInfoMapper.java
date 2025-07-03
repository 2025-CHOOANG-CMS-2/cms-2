package kr.ac.dhuniv.ncs.mapper;

import kr.ac.dhuniv.ncs.dto.CompletionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NcsCmpInfoMapper {
    /** 이수 관리 목록 조회 */
    List<CompletionDto> findCompletionList(Map<String, Object> params);

    /** 이수 관리 목록 개수 조회 */
    int countCompletionList(Map<String, Object> params);

    /** 이수 상태로 변경 */
    int updateStatusToComplete(@Param("cmpId") Long cmpId);
}
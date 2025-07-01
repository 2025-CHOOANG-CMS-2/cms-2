package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.dto.CounselingResultItemDto;
import kr.ac.dhuniv.counsel.dto.PageDto;
import kr.ac.dhuniv.counsel.dto.WriteResultRequestDto;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CounselorService {

    /**
     * 상담사가 자신의 상담 결과 목록을 조회합니다.
     */
    PageDto<CounselingResultItemDto> getCounselingResults(String counselorId, List<String> statuses, String period, String type, Pageable pageable);

    /**
     * [추가] '결과 수정' 시, 모달에 채워넣을 상세 데이터를 조회합니다.
     */
    CounselingResultItemDto getCounselingResultDetail(Long resultId);

    /**
     * [추가] 상담 결과를 처음으로 작성(저장)합니다.
     */
    void writeCounselingResult(String counselorId, WriteResultRequestDto requestDto);

    /**
     * [추가] 이미 작성된 상담 결과를 수정합니다.
     */
    void updateCounselingResult(Long resultId, WriteResultRequestDto requestDto, String counselorId);
    
    
}

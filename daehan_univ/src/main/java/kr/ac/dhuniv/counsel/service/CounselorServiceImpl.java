package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.CnslAply;
import kr.ac.dhuniv.counsel.domain.CnslRslt;
import kr.ac.dhuniv.counsel.dto.CounselingResultItemDto;
import kr.ac.dhuniv.counsel.dto.PageDto;
import kr.ac.dhuniv.counsel.dto.WriteResultRequestDto;
import kr.ac.dhuniv.counsel.mapper.CounselorResultMapper;
import kr.ac.dhuniv.counsel.repository.CnslAplyRepository;
import kr.ac.dhuniv.counsel.repository.CnslRsltRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements CounselorService {

    private final CounselorResultMapper counselorResultMapper;
    private final CnslAplyRepository cnslAplyRepository;
    private final CnslRsltRepository cnslRsltRepository;

    @Override
    @Transactional(readOnly = true)
    public PageDto<CounselingResultItemDto> getCounselingResults(String counselorId, List<String> statuses, String period, String type, Pageable pageable) {
        long totalElements = counselorResultMapper.countByCounselorAndFilters(counselorId, statuses, period, type);
        List<CounselingResultItemDto> content = counselorResultMapper.findByCounselorAndFilters(counselorId, statuses, period, type, pageable.getOffset(), pageable.getPageSize());
        return new PageDto<>(content, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public CounselingResultItemDto getCounselingResultDetail(Long resultId) {
        return counselorResultMapper.findDetailByResultId(resultId)
                .orElseThrow(() -> new IllegalArgumentException("상담 결과 정보를 찾을 수 없습니다. ID: " + resultId));
    }

    @Override
    @Transactional
    public void writeCounselingResult(String counselorId, WriteResultRequestDto requestDto) {
        CnslAply aply = cnslAplyRepository.findById(requestDto.getApplyId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담 신청입니다."));

        // [수정] EmplInfo의 User 객체를 통해 ID를 가져오도록 변경
        if (!aply.getEmployee().getUser().getUserId().equals(counselorId)) {
            throw new SecurityException("결과를 작성할 권한이 없습니다.");
        }
        if ("COMPLETED".equals(aply.getStatusCode())) {
            throw new IllegalStateException("이미 결과가 작성된 상담입니다.");
        }

        CnslRslt newResult = CnslRslt.builder()
                .counselingApplication(aply) // [수정] 필드명 counselingApplication 으로 변경
                .cnslRsltId(aply.getCnslAplyId()) // cnsl_rslt 테이블의 비즈니스 키, 필요시 로직 수정
                .counselingContent(requestDto.getCounselingContent()) // [수정] 필드명 counselingContent 로 변경
                .counselingDateTime(LocalDateTime.now())
                .build();
        cnslRsltRepository.save(newResult);

        aply.setStatusCode("COMPLETED");
    }

    @Override
    @Transactional
    public void updateCounselingResult(Long resultId, WriteResultRequestDto requestDto, String counselorId) {
        CnslRslt result = cnslRsltRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담 결과입니다."));

        // [수정] 필드명 및 User 객체를 통해 ID를 가져오도록 변경
        if (!result.getCounselingApplication().getEmployee().getUser().getUserId().equals(counselorId)) {
            throw new SecurityException("결과를 수정할 권한이 없습니다.");
        }

        // [수정] setter 메소드 이름 변경
        result.setCounselingContent(requestDto.getCounselingContent());
    }
    
    
}

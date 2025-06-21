package kr.ac.dhuniv.ncs.service;

import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NcsPrgInfoService {
	   /**
     * 프로그램 목록 조회 (키워드, 카테고리 필터 + 페이징)
     */
    Page<NcsPrgInfo> getPrograms(String keyword, Long categoryId, Pageable pageable);

    /**
     * 단건 조회
     */
    NcsPrgInfo getProgram(Long id);

    /**
     * 등록
     */
    NcsPrgInfo createProgram(NcsPrgInfo program);

    /**
     * 수정
     */
    NcsPrgInfo updateProgram(Long id, NcsPrgInfo program);

    /**
     * 삭제
     */
    void deleteProgram(Long id);
}

package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstListDTO;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstRequestDTO;
import kr.ac.dhuniv.core_cpt.service.CoreCptQstService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ✅ CoreCptQstController
 * - 진단 문항 관리 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class CoreCptQstController {

    private final CoreCptQstService service;
    /**
     * ✅ 조건 기반 + 페이징 문항 목록 조회 API
     *
     * @param topCptId 상위 역량 ID (선택)
     * @param subCptId 하위 역량 ID (선택)
     * @param keyword  검색어 (선택)
     * @param pageable 페이징 정보 (page, size, sort)
     * @return Page 객체 (문항 + 페이징 정보)
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<CoreCptQstListDTO>> filterQuestions(
            @RequestParam(required = false) Long topCptId,
            @RequestParam(required = false) Long subCptId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "qstId") Pageable pageable
    ) {
        // 서비스에서 Page 형태로 반환
        Page<CoreCptQstListDTO> result = service.filterQuestionList(topCptId, subCptId, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 진단 문항 등록 API
     * @param dto 문항 + 선택지 정보
     * @return 상태코드 200 OK
     */
    @PostMapping
    public ResponseEntity<Void> addQuestion(@RequestBody CoreCptQstRequestDTO dto) {
        service.addQuestion(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ 진단 문항 삭제 API
     * @param id 문항 ID
     * @return 상태코드 200 OK
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        service.deleteQuestion(id);
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ 진단 문항 목록 조회 API
     * @return 문항 리스트
     */
    @GetMapping
    public ResponseEntity<List<CoreCptQstListDTO>> getQuestions() {
        return ResponseEntity.ok(service.getQuestionList());
    }

    /**
     * ✅ 문항 코드 생성 API
     * - 하위 역량 ID 기반으로 다음 문항 코드 제공
     * @param subCptId 하위 역량 ID
     * @return String 형태의 다음 문항 코드
     */
    @GetMapping("/next-code")
    public ResponseEntity<String> generateNextQstCode(@RequestParam Long subCptId) {
        String nextCode = service.generateNextQstCode(subCptId);
        return ResponseEntity.ok(nextCode);
    }
}


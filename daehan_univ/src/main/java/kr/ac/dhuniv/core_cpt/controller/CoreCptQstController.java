package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstListDTO;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstRequestDTO;
import kr.ac.dhuniv.core_cpt.service.CoreCptQstService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
     * ✅ 검색/필터 조건 기반 문항 목록 조회 API
     * - 클라이언트에서 전달한 조건(topCptId, subCptId, status, keyword)에 맞는 문항 반환
     *
     * @param topCptId 상위 역량 ID (nullable)
     * @param subCptId 하위 역량 ID (nullable)
     * @param status 상태 필터 (nullable) — 현재 DB 컬럼 없으면 미사용
     * @param keyword 문항 내용 검색어 (nullable)
     * @return 조건에 맞는 문항 리스트
     */
    @GetMapping("/filter")
    public ResponseEntity<?> filterQuestions(
            @RequestParam(required = false) Long topCptId,
            @RequestParam(required = false) Long subCptId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("qstOrd").ascending());
        Page<CoreCptQstListDTO> resultPage = service.filterQuestionList(topCptId, subCptId, pageable, keyword);
        return ResponseEntity.ok(Map.of(
                "questions", resultPage.getContent(),
                "pageInfo", Map.of(
                        "currentPage", resultPage.getNumber() + 1,
                        "totalPages", resultPage.getTotalPages(),
                        "totalElements", resultPage.getTotalElements()
                )
        ));
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


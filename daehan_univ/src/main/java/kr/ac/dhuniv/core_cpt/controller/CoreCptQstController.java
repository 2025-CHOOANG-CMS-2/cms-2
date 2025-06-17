package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstListDTO;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstRequestDTO;
import kr.ac.dhuniv.core_cpt.service.CoreCptQstService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


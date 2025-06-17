package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
import kr.ac.dhuniv.core_cpt.dto.comment.CoreCptCommentRequestDTO;
import kr.ac.dhuniv.core_cpt.service.CoreCptCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/competencies/")
@RequiredArgsConstructor
public class CoreCptCommentController {
    private final CoreCptCommentService commentService;

    @GetMapping("/comments/{cciId}")
    public ResponseEntity<String> getCommentByScore(
            @PathVariable("cciId") Long cciId,
            @RequestParam int score) {
        String comment = commentService.getCommentByScore(cciId, score);
        return ResponseEntity.ok(comment);
    }
    /**
     * ✅ 점수 구간 코멘트 등록
     * @param cciId 상위 역량 ID (PathVariable)
     * @param dto 클라이언트에서 전달받은 점수 구간 + 코멘트 DTO
     * @return 등록된 CoreCptCommentTemplate 객체
     */
    @PostMapping("/{cciId}/comments")
    public ResponseEntity<CoreCptCommentTemplate> registerComment(
            @PathVariable Long cciId,
            @RequestBody CoreCptCommentRequestDTO dto) {

        // 서비스에서 등록 로직 처리
        CoreCptCommentTemplate saved = commentService.registerComment(cciId, dto);

        // 200 OK + 등록된 엔티티 반환
        return ResponseEntity.ok(saved);
    }
}

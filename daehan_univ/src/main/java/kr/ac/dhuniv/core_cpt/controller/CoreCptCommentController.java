package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
import kr.ac.dhuniv.core_cpt.dto.comment.CoreCptCommentRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.comment.CoreCptCommentResponseDTO;
import kr.ac.dhuniv.core_cpt.service.CoreCptCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competencies/")
@RequiredArgsConstructor
public class CoreCptCommentController {
    private final CoreCptCommentService commentService;

    @GetMapping("/comments/detail/{cciId}")
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
    /**
     * ✅ 특정 상위 역량에 등록된 점수 구간 코멘트 목록 조회
     * @param cciId 상위 역량 PK (core_cpt_info. cci_id)
     * @return 코멘트 DTO 리스트
     */
    @GetMapping("/comments/{cciId}")
    public ResponseEntity<List<CoreCptCommentResponseDTO>> getCommentsByCompetency(@PathVariable("cciId") Long cciId) {
        // 서비스에서 해당 상위 역량의 코멘트 목록 조회
        List<CoreCptCommentResponseDTO> comments = commentService.getCommentsByCompetency(cciId);
        return ResponseEntity.ok(comments); // 조회된 코멘트 목록을 응답
    }

    @PutMapping("/{cciId}/comments/{commentId}")
    public ResponseEntity<CoreCptCommentResponseDTO> updateComment(
            @PathVariable("cciId") Long cciId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CoreCptCommentResponseDTO dto) {

        // 서비스에 위임
        CoreCptCommentResponseDTO updated = commentService.updateComment(cciId, commentId, dto);
        return ResponseEntity.ok(updated);
    }
}

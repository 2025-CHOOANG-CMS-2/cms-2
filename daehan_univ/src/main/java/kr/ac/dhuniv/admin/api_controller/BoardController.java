package kr.ac.dhuniv.admin.api_controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kr.ac.dhuniv.admin.service.BoardService;
import kr.ac.dhuniv.notice.dto.BoardResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards") // 게시판 (카테고리) 관련 API 경로
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // TODO: 로그인한 사용자 ID를 가져오는 로직 추가 (현재는 임시로 "ADMIN" 사용)
    private String getCurrentCreatedBy() {
        // 실제 구현에서는 Spring Security 등을 사용하여 로그인한 관리자 ID를 가져와야 합니다.
        return "ADMIN"; // 임시 관리자 ID
    }

    /**
     * 모든 게시판(카테고리) 목록을 조회합니다.
     * GET /api/admin/boards
     */
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAllBoards() {
        List<BoardResponse> response = boardService.getAllBoards();
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 ID의 게시판(카테고리)을 조회합니다.
     * GET /api/admin/boards/{boardId}
     */
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long boardId) {
        BoardResponse response = boardService.getBoardById(boardId);
        return ResponseEntity.ok(response);
    }

    /**
     * 새로운 게시판(카테고리)을 생성합니다.
     * POST /api/admin/boards
     */
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestParam String boardName, @RequestParam String boardType) {
        String createdBy = getCurrentCreatedBy(); // 현재 로그인한 관리자 ID
        BoardResponse response = boardService.createBoard(boardName, boardType, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게시판(카테고리) 정보를 업데이트합니다.
     * PUT /api/admin/boards/{boardId}
     */
    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable Long boardId,
            @RequestParam(required = false) String newBoardName,
            @RequestParam(required = false) String newBoardType) {
        String modifiedBy = getCurrentCreatedBy(); // 현재 로그인한 관리자 ID
        BoardResponse response = boardService.updateBoard(boardId, newBoardName, newBoardType, modifiedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 게시판(카테고리)을 삭제합니다.
     * DELETE /api/admin/boards/{boardId}
     */
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId) {
        boardService.deleteBoard(boardId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
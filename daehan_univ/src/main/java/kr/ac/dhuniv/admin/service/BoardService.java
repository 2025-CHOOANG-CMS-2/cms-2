package kr.ac.dhuniv.admin.service;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.dhuniv.notice.domain.Board;
import kr.ac.dhuniv.notice.dto.BoardResponse;
import kr.ac.dhuniv.notice.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 모든 게시판 (카테고리) 목록을 조회합니다.
     * @return 게시판 응답 DTO 목록
     */
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll().stream()
                .map(BoardResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID의 게시판 (카테고리)을 조회합니다.
     * @param boardId 게시판 ID
     * @return 조회된 게시판의 응답 DTO
     */
    public BoardResponse getBoardById(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시판을 찾을 수 없습니다: " + boardId));
        return BoardResponse.from(board);
    }

    /**
     * 새로운 게시판 (카테고리)을 생성합니다.
     * @param boardName 생성할 게시판 이름
     * @param boardType 게시판 타입 (예: "공지사항", "자유게시판")
     * @param createdBy 생성자 ID
     * @return 생성된 게시판의 응답 DTO
     */
    @Transactional
    public BoardResponse createBoard(String boardName, String boardType, String createdBy) {
        // 게시판 이름 중복 확인 (선택 사항)
        Optional<Board> existingBoardByName = boardRepository.findByBoardName(boardName);
        if (existingBoardByName.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 게시판 이름입니다: " + boardName);
        }

        Board board = Board.builder()
                .boardName(boardName)
                .boardType(boardType)
                .createdBy(createdBy)
                .build();
        Board savedBoard = boardRepository.save(board);
        return BoardResponse.from(savedBoard);
    }

    /**
     * 게시판 (카테고리) 정보를 업데이트합니다.
     * @param boardId 업데이트할 게시판 ID
     * @param newBoardName 새로운 게시판 이름 (nullable)
     * @param newBoardType 새로운 게시판 타입 (nullable)
     * @param modifiedBy 수정자 ID
     * @return 업데이트된 게시판의 응답 DTO
     */
    @Transactional
    public BoardResponse updateBoard(Long boardId, String newBoardName, String newBoardType, String modifiedBy) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시판을 찾을 수 없습니다: " + boardId));

        if (newBoardName != null && !newBoardName.trim().isEmpty()) {
            Optional<Board> existingBoard = boardRepository.findByBoardName(newBoardName);
            if (existingBoard.isPresent() && !existingBoard.get().getId().equals(boardId)) {
                throw new IllegalArgumentException("이미 존재하는 게시판 이름입니다: " + newBoardName);
            }
            board.updateBoardName(newBoardName, modifiedBy);
        }

        if (newBoardType != null && !newBoardType.trim().isEmpty()) {
            board.updateBoardType(newBoardType, modifiedBy);
        }

        return BoardResponse.from(board);
    }

    /**
     * 게시판 (카테고리)을 삭제합니다.
     * (참고: DDL의 FK 설정에 따라 게시판 삭제 시 해당 게시판의 게시글도 함께 삭제되거나(CASCADE) NULL로 설정됩니다(SET NULL).
     * 여기서는 ON DELETE CASCADE가 설정되어 있어 게시판 삭제 시 게시글도 함께 삭제됩니다.)
     *
     * @param boardId 삭제할 게시판 ID
     */
    @Transactional
    public void deleteBoard(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new EntityNotFoundException("해당 ID의 게시판을 찾을 수 없습니다: " + boardId);
        }
        boardRepository.deleteById(boardId);
    }

    /**
     * "공지사항" 타입의 게시판 ID를 조회합니다.
     * (공지사항 기능에서 특정 게시판을 사용한다고 가정할 때 유용)
     * @return "공지사항" 게시판의 ID
     * @throws EntityNotFoundException "공지사항" 게시판을 찾을 수 없을 경우
     */
    public Long getNoticeBoardId() {
        return boardRepository.findByBoardType("공지사항")
                .orElseThrow(() -> new EntityNotFoundException("'공지사항' 타입의 게시판을 찾을 수 없습니다. 먼저 생성해주세요."))
                .getId();
    }
}
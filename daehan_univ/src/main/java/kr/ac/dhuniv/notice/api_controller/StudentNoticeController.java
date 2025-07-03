package kr.ac.dhuniv.notice.api_controller;

import kr.ac.dhuniv.admin.service.PostService;
import kr.ac.dhuniv.notice.dto.PostResponse;
import kr.ac.dhuniv.notice.dto.PostStatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// import java.time.LocalDate; // 이 컨트롤러에서 직접 사용되지 않으므로 제거 가능

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/posts")
public class StudentNoticeController {

    private final PostService postService;

    /**
     * 학생용 공지사항 목록을 페이징, 검색, 필터링하여 조회합니다.
     * 관리자 페이지의 getPostsPagedAndFiltered를 재활용합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @param sortBy 정렬 기준 필드 (예: createdAt, viewCount, title)
     * @param direction 정렬 방향 (asc, desc)
     * @param searchTitle 제목 검색어 (선택 사항)
     * @param boardId 게시판 ID (선택 사항)
     * @param isImportant 중요 공지사항 필터 (선택 사항)
     * @return 페이징된 공지사항 목록 (PostResponse DTO)
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getStudentNotices(
            @RequestParam(name = "page", defaultValue = "0") int page, // ⭐ name 속성 추가 ⭐
            @RequestParam(name = "size", defaultValue = "10") int size, // ⭐ name 속성 추가 ⭐
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy, // ⭐ name 속성 추가 ⭐
            @RequestParam(name = "direction", defaultValue = "desc") String direction, // ⭐ name 속성 추가 ⭐
            @RequestParam(name = "searchTitle", required = false) String searchTitle, // ⭐ name 속성 추가 ⭐
            @RequestParam(name = "boardId", required = false) Long boardId, // ⭐ name 속성 추가 ⭐
            @RequestParam(name = "isImportant", required = false) Boolean isImportant) { // ⭐ name 속성 추가 ⭐

        Boolean isPublished = true; // 학생 페이지에서는 '게시됨' 상태의 공지사항만 보여주므로 isPublished는 항상 true로 고정

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostResponse> notices = postService.getPostsPagedAndFiltered(
                pageable, searchTitle, boardId, isPublished, isImportant);

        return ResponseEntity.ok(notices);
    }

    /**
     * 특정 ID의 공지사항 상세 정보를 조회합니다. 조회 시 조회수를 1 증가시킵니다.
     * 관리자 페이지의 getPostById를 재활용합니다.
     *
     * @param postId 공지사항 ID
     * @return 공지사항 상세 정보 (PostResponse DTO)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getStudentNoticeDetail(@PathVariable("postId") Long postId) { // ⭐ @PathVariable에 name 속성 추가 ⭐
        PostResponse post = postService.getPostById(postId); // 조회수 증가 로직 포함
        return ResponseEntity.ok(post);
    }

    /**
     * 학생용 대시보드 통계 데이터를 조회합니다.
     * 관리자 페이지의 getPostStatistics를 재활용합니다.
     *
     * @return 공지사항 통계 응답 DTO
     */
    @GetMapping("/statistics")
    public ResponseEntity<PostStatisticsResponse> getStudentNoticeStatistics() {
        PostStatisticsResponse stats = postService.getPostStatistics();
        return ResponseEntity.ok(stats);
    }
}

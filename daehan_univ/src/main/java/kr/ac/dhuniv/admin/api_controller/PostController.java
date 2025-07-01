package kr.ac.dhuniv.admin.api_controller;

import jakarta.validation.Valid;
import kr.ac.dhuniv.admin.service.PostService;
import kr.ac.dhuniv.admin.service.AdminEmpService; // ⭐ 추가: AdminEmpService import ⭐
import kr.ac.dhuniv.empl_info.dto.EmplInfoDto; // ⭐ 추가: EmplInfoDto import ⭐
import kr.ac.dhuniv.notice.dto.PostCreateRequest;
import kr.ac.dhuniv.notice.dto.PostResponse;
import kr.ac.dhuniv.notice.dto.PostStatisticsResponse;
import kr.ac.dhuniv.notice.dto.PostUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.stream.Collectors; // Collectors 에러 해결
import org.springframework.data.domain.PageImpl; // PageImpl 에러 해결
import org.springframework.data.domain.Page; // Page 인터페이스도 함께 필요할 수 있습니다.

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j; // ⭐ 추가: 로깅을 위해 Slf4j import ⭐

import java.util.List;

@RestController
@RequestMapping("/api/admin/posts") // 게시글 관련 API 경로
@RequiredArgsConstructor
@Slf4j // ⭐ 추가: 로깅을 위해 Slf4j 어노테이션 추가 ⭐
public class PostController {

    private final PostService postService;
    private final AdminEmpService adminEmpService; // ⭐ 추가: AdminEmpService 주입 ⭐

    // TODO: 로그인한 사용자 ID를 가져오는 로직 추가 (현재는 임시로 "100" 사용)
    private String getCurrentWriterEmplId() {
        // 실제 구현에서는 Spring Security 등을 사용하여 로그인한 사용자의 ID를 가져와야 합니다.
        // 예를 들어: SecurityContextHolder.getContext().getAuthentication().getName(); 등을 통해 교직원 ID를 얻습니다.
        return "100"; // 임시 교직원 ID (String)
    }

 // ⭐ 추가: 현재 로그인한 사용자의 상세 정보를 가져오는 헬퍼 메서드 ⭐
    private EmplInfoDto getCurrentWriterInfo() {
        String emplId = getCurrentWriterEmplId(); // 현재 로그인한 사용자의 사번을 가져오는 메서드
        try {
            // adminEmpService를 통해 해당 사번의 교직원 정보를 가져옵니다.
            // 이 EmplInfoDto 안에는 이미 STAFF_NM과 DEPT_NM이 채워져 있을 것입니다.
            return adminEmpService.getEmployeeByEmplNo(emplId);
        } catch (IllegalArgumentException e) {
            log.warn("WARN: 현재 로그인한 교직원 정보(ID: {})를 찾을 수 없습니다. 기본값을 반환합니다. {}", emplId, e.getMessage());
            // 교직원 정보를 찾을 수 없을 경우를 대비한 기본값 또는 예외 처리
            // 만약 adminEmpService.getEmployeeByEmplNo(emplId)에서 null이나 Optional.empty()가 반환될 경우,
            // 이곳에서 기본값을 가진 EmplInfoDto를 빌더 패턴으로 생성하여 반환합니다.
            return EmplInfoDto.builder()
                    .STAFF_NO(emplId)
                    .STAFF_NM("알 수 없음") // 서비스에서 가져오지 못했을 경우의 기본 이름
                    .DEPT_NM("알 수 없음") // 서비스에서 가져오지 못했을 경우의 기본 부서
                    .build();
        }
    }

    /**
     * 새로운 게시글(공지사항)을 생성합니다.
     * POST /api/admin/posts
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        String writerEmplId = getCurrentWriterEmplId();
        PostResponse response = postService.createPost(request, writerEmplId);

        // ⭐ 추가: PostResponse에 작성자 이름 및 부서 정보 설정 ⭐
        EmplInfoDto writerInfo = getCurrentWriterInfo();
        response.setWriterName(writerInfo.getSTAFF_NM());
        response.setWriterDeptName(writerInfo.getDEPT_NM());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 게시글(공지사항)을 조회합니다.
     * GET /api/admin/posts/{postId}
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable("postId") Long postId) {
        PostResponse response = postService.getPostById(postId);

        // ⭐ 추가: 조회된 게시글의 작성자 이름 및 부서 정보 설정 ⭐
        try {
            EmplInfoDto writerInfo = adminEmpService.getEmployeeByEmplNo(response.getWriterEmplId());
            response.setWriterName(writerInfo.getSTAFF_NM());
            response.setWriterDeptName(writerInfo.getDEPT_NM());
        } catch (IllegalArgumentException e) {
            log.warn("WARN: 게시글(ID: {})의 작성자 정보(ID: {})를 찾을 수 없습니다. 기본값을 반환합니다. {}", postId, response.getWriterEmplId(), e.getMessage());
            response.setWriterName("알 수 없음");
            response.setWriterDeptName("알 수 없음");
        }
        // ⭐ 옵션: 수정자 정보도 필요하다면 아래와 같이 추가 가능 ⭐
        // if (response.getModifiedBy() != null) {
        //     try {
        //         EmplInfoDto modifierInfo = adminEmpService.getEmployeeByEmplNo(response.getModifiedBy());
        //         // PostResponse에 modifiedByName 필드를 추가하고 여기서 설정
        //         // response.setModifiedByName(modifierInfo.getSTAFF_NM());
        //     } catch (IllegalArgumentException e) {
        //         // response.setModifiedByName("알 수 없음");
        //     }
        // }

        return ResponseEntity.ok(response);
    }

    /**
     * 모든 게시글(공지사항) 목록을 조회합니다. (페이징, 검색, 필터링 기능 포함)
     * GET /api/admin/posts?page={page}&size={size}&sortBy={field}&direction={asc/desc}&searchTitle={title}&boardId={id}&isPublished={boolean}&isImportant={boolean}
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @RequestParam(name = "searchTitle", required = false) String searchTitle,
            @RequestParam(name = "boardId", required = false) Long boardId,
            @RequestParam(name = "isPublished", required = false) Boolean isPublished,
            @RequestParam(name = "isImportant", required = false) Boolean isImportant
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PostResponse> postsPage = postService.getPostsPagedAndFiltered(
            pageable, searchTitle, boardId, isPublished, isImportant
        );

        // ⭐ 추가: 페이지 내 각 게시글의 작성자 이름 및 부서 정보 설정 ⭐
        List<PostResponse> content = postsPage.getContent().stream().map(postResponse -> {
            try {
                EmplInfoDto writerInfo = adminEmpService.getEmployeeByEmplNo(postResponse.getWriterEmplId());
                postResponse.setWriterName(writerInfo.getSTAFF_NM());
                postResponse.setWriterDeptName(writerInfo.getDEPT_NM());
            } catch (IllegalArgumentException e) {
                log.warn("WARN: 목록 내 게시글(ID: {})의 작성자 정보(ID: {})를 찾을 수 없습니다. {}", postResponse.getPostId(), postResponse.getWriterEmplId(), e.getMessage());
                postResponse.setWriterName("알 수 없음");
                postResponse.setWriterDeptName("알 수 없음");
            }
            return postResponse;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(content, pageable, postsPage.getTotalElements()));
    }

    /**
     * 게시글(공지사항)을 업데이트합니다.
     * PUT /api/admin/posts/{postId}
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        String modifiedByEmplId = getCurrentWriterEmplId();
        PostResponse response = postService.updatePost(postId, request, modifiedByEmplId);

        // ⭐ 추가: 업데이트된 PostResponse에 작성자 이름 및 부서 정보 설정 ⭐
        try {
            EmplInfoDto writerInfo = adminEmpService.getEmployeeByEmplNo(response.getWriterEmplId());
            response.setWriterName(writerInfo.getSTAFF_NM());
            response.setWriterDeptName(writerInfo.getDEPT_NM());
        } catch (IllegalArgumentException e) {
            log.warn("WARN: 업데이트된 게시글(ID: {})의 작성자 정보(ID: {})를 찾을 수 없습니다. {}", response.getPostId(), response.getWriterEmplId(), e.getMessage());
            response.setWriterName("알 수 없음");
            response.setWriterDeptName("알 수 없음");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 게시글(공지사항)을 삭제합니다.
     * DELETE /api/admin/posts/{postId}
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 대시보드에 표시할 공지사항 통계 데이터를 조회합니다.
     * GET /api/admin/posts/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<PostStatisticsResponse> getPostStatistics() {
        PostStatisticsResponse statistics = postService.getPostStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 여러 개의 게시글(공지사항)을 일괄 삭제합니다.
     * DELETE /api/admin/posts/bulk-delete
     * @param postIds 삭제할 게시글 ID 목록
     */
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Void> deletePosts(@RequestBody List<Long> postIds) {
        postService.deletePostsByIds(postIds);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * 특정 게시글의 게시 상태를 토글합니다.
     * PUT /api/admin/posts/{postId}/publish
     * @param postId 게시글 ID
     * @param isPublished 게시 여부 (true: 게시, false: 비게시)
     */
    @PutMapping("/{postId}/publish")
    public ResponseEntity<PostResponse> togglePublishStatus(
            @PathVariable("postId") Long postId,
            @RequestBody Boolean isPublished) { // isPublished 값을 직접 받음
        String modifiedByEmplId = getCurrentWriterEmplId(); // 현재 로그인한 사용자 ID (String)
        PostResponse response = postService.updatePostPublishStatus(postId, isPublished, modifiedByEmplId);
        
        // ⭐ 추가: 상태 토글 후 PostResponse에 작성자 이름 및 부서 정보 설정 ⭐
        try {
            EmplInfoDto writerInfo = adminEmpService.getEmployeeByEmplNo(response.getWriterEmplId());
            response.setWriterName(writerInfo.getSTAFF_NM());
            response.setWriterDeptName(writerInfo.getDEPT_NM());
        } catch (IllegalArgumentException e) {
            log.warn("WARN: 상태 토글된 게시글(ID: {})의 작성자 정보(ID: {})를 찾을 수 없습니다. {}", response.getPostId(), response.getWriterEmplId(), e.getMessage());
            response.setWriterName("알 수 없음");
            response.setWriterDeptName("알 수 없음");
        }

        return ResponseEntity.ok(response);
    }
}
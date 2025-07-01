package kr.ac.dhuniv.admin.service;

import jakarta.persistence.EntityNotFoundException;
import kr.ac.dhuniv.notice.domain.Attachment;
import kr.ac.dhuniv.notice.domain.Board;
import kr.ac.dhuniv.notice.domain.Post;
import kr.ac.dhuniv.notice.dto.PostCreateRequest;
import kr.ac.dhuniv.notice.dto.PostResponse;
import kr.ac.dhuniv.notice.dto.PostStatisticsResponse;
import kr.ac.dhuniv.notice.dto.PostUpdateRequest;
import kr.ac.dhuniv.notice.repository.AttachmentRepository;
import kr.ac.dhuniv.notice.repository.BoardRepository;
import kr.ac.dhuniv.notice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate; 

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final AttachmentRepository attachmentRepository;

    /**
     * 새로운 게시글 (공지사항)을 생성합니다.
     *
     * @param request 게시글 생성 요청 DTO
     * @param writerEmplId 게시글을 생성한 교직원 ID (String 타입)
     * @return 생성된 게시글의 응답 DTO
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest request, String writerEmplId) {
        // 1. 게시판 (Board) 조회 및 유효성 검사
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시판을 찾을 수 없습니다: " + request.getBoardId()));

        // 2. Post 엔티티 생성
        Post post = Post.builder()
                .board(board)
                .postNo(request.getPostNo())
                .title(request.getTitle())
                .content(request.getContent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isImportant(request.getIsImportant())
                .isPublished(request.getIsPublished())
                .writerEmplId(writerEmplId) // 작성자 교직원 ID 주입 (String)
                .viewCount(0) // 초기 조회수 0 설정
                .modifiedBy(writerEmplId) // modifiedBy 필드에 작성자 ID 설정 (초기값)
                .createdAt(LocalDateTime.now()) // ⭐ 추가: 현재 시간을 등록일로 직접 설정 ⭐
                .build();

        // 3. 첨부 파일 연결 (UUID를 통해 기존 파일 엔티티를 찾아서 연결)
        if (request.getAttachmentUuids() != null && !request.getAttachmentUuids().isEmpty()) {
            List<Attachment> attachments = request.getAttachmentUuids().stream()
                    .map(uuid -> attachmentRepository.findByUuid(uuid.toString())
                            .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다: " + uuid)))
                    .collect(Collectors.toList());

            post.setAttachments(new HashSet<>(attachments)); // Post에 Attachment 설정
        }

        // 4. 게시글 저장
        Post savedPost = postRepository.save(post);

        // 5. 응답 DTO 변환 및 반환
        return PostResponse.from(savedPost);
    }

    /**
     * 특정 ID의 게시글을 조회합니다. 조회 시 조회수를 1 증가시킵니다.
     *
     * @param postId 게시글 ID
     * @return 조회된 게시글의 응답 DTO
     */
    @Transactional
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId));

        post.incrementViewCount(); // 조회수 증가

        return PostResponse.from(post);
    }

    /**
     * 모든 게시글 목록을 조회합니다.
     * (여기서는 간단히 모든 게시글을 반환하지만, 실제로는 페이징/검색/필터링 로직이 추가됩니다.)
     *
     * @return 게시글 응답 DTO 목록
     */
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 목록을 페이징, 검색, 필터링하여 조회합니다.
     *
     * @param pageable 페이징 및 정렬 정보
     * @param searchTitle 제목 검색어 (선택 사항)
     * @param boardId 게시판 ID 필터 (선택 사항)
     * @param isPublished 게시 상태 필터 (선택 사항)
     * @param isImportant 중요 공지사항 필터 (선택 사항)
     * @return 페이징된 게시글 응답 DTO 목록
     */
    public Page<PostResponse> getPostsPagedAndFiltered(
            Pageable pageable,
            String searchTitle,
            Long boardId,
            Boolean isPublished,
            Boolean isImportant) {

        Page<Post> postsPage;

        // 모든 필터 파라미터가 null인 경우를 대비하여 기본 findAll을 호출
        if (searchTitle == null && boardId == null && isPublished == null && isImportant == null) {
            postsPage = postRepository.findAll(pageable);
        } else {
            if (searchTitle != null && !searchTitle.trim().isEmpty()) {
                if (boardId != null && isPublished != null && isImportant != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndBoardIdAndIsPublishedAndIsImportant(
                        searchTitle, boardId, isPublished, isImportant, pageable);
                } else if (boardId != null && isPublished != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndBoardIdAndIsPublished(
                        searchTitle, boardId, isPublished, pageable);
                } else if (boardId != null && isImportant != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndBoardIdAndIsImportant(
                        searchTitle, boardId, isImportant, pageable);
                } else if (isPublished != null && isImportant != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndIsPublishedAndIsImportant(
                        searchTitle, isPublished, isImportant, pageable);
                } else if (boardId != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndBoardId(
                        searchTitle, boardId, pageable);
                } else if (isPublished != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndIsPublished(
                        searchTitle, isPublished, pageable);
                } else if (isImportant != null) {
                    postsPage = postRepository.findByTitleContainingIgnoreCaseAndIsImportant(
                        searchTitle, isImportant, pageable);
                } else {
                    postsPage = postRepository.findByTitleContainingIgnoreCase(searchTitle, pageable);
                }
            } else { // 검색어가 없는 경우
                if (boardId != null && isPublished != null && isImportant != null) {
                    postsPage = postRepository.findByBoardIdAndIsPublishedAndIsImportant(
                        boardId, isPublished, isImportant, pageable);
                } else if (boardId != null && isPublished != null) {
                    postsPage = postRepository.findByBoardIdAndIsPublished(
                        boardId, isPublished, pageable);
                } else if (boardId != null && isImportant != null) {
                    postsPage = postRepository.findByBoardIdAndIsImportant(
                        boardId, isImportant, pageable);
                } else if (isPublished != null && isImportant != null) {
                    postsPage = postRepository.findByIsPublishedAndIsImportant(
                        isPublished, isImportant, pageable);
                } else if (boardId != null) {
                    postsPage = postRepository.findByBoardId(boardId, pageable);
                } else if (isPublished != null) {
                    postsPage = postRepository.findByIsPublished(isPublished, pageable);
                } else if (isImportant != null) {
                    postsPage = postRepository.findByIsImportant(isImportant, pageable);
                } else {
                    postsPage = postRepository.findAll(pageable); // 기본 모든 게시글 페이징
                }
            }
        }
        
        List<PostResponse> content = postsPage.getContent().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, postsPage.getTotalElements());
    }


    /**
     * 게시글을 업데이트합니다.
     *
     * @param request 게시글 업데이트 요청 DTO
     * @param modifiedByEmplId 게시글을 수정한 교직원 ID (String 타입)
     * @return 업데이트된 게시글의 응답 DTO
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String modifiedByEmplId) { // ⭐ 여기에 Long postId를 추가합니다! ⭐
        // 1. 게시글 조회: request.getPostId() 대신 파라미터로 받은 postId를 사용합니다.
        Post existingPost = postRepository.findById(postId) // ⭐ 이 부분을 이렇게 수정합니다. ⭐
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId)); // ⭐ 에러 메시지도 postId를 사용하도록 변경합니다. ⭐

        // 2. 게시판 (Board) 조회 (이 부분은 기존과 동일)
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시판을 찾을 수 없습니다: " + request.getBoardId()));

        // 3. Post 엔티티 업데이트 (Dirty Checking)
        existingPost.update(
                board,
                request.getPostNo(),
                request.getTitle(),
                request.getContent(),
                request.getWriterEmplId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsImportant(),
                request.getIsPublished(),
                modifiedByEmplId // 수정자 정보 주입 (String)
        );

        // 4. 첨부 파일 업데이트: 기존 연결을 끊고 새로운 파일 연결 (UUID 사용)
        existingPost.getAttachments().clear();
        if (request.getAttachmentUuids() != null && !request.getAttachmentUuids().isEmpty()) {
            List<Attachment> attachmentsToLink = request.getAttachmentUuids().stream()
                    .map(uuid -> attachmentRepository.findByUuid(uuid.toString())
                            .orElseThrow(() -> new EntityNotFoundException("파일을 찾을 수 없습니다: " + uuid)))
                    .collect(Collectors.toList());
            attachmentsToLink.forEach(file -> file.setPost(existingPost));
            existingPost.setAttachments(new HashSet<>(attachmentsToLink));
        }

        return PostResponse.from(existingPost);
    }


    /**
     * 특정 ID의 게시글을 삭제합니다.
     *
     * @param postId 삭제할 게시글 ID
     */
    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId);
        }
        postRepository.deleteById(postId);
    }

    /**
     * 여러 개의 게시글을 ID 목록으로 삭제합니다.
     * @param postIds 삭제할 게시글 ID 목록
     */
    @Transactional
    public void deletePostsByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return; // 삭제할 ID가 없으면 아무것도 하지 않음
        }
        postRepository.deleteAllByIdInBatch(postIds);
    }

    /**
     * 특정 게시글의 게시 상태를 업데이트합니다.
     * @param postId 게시글 ID
     * @param isPublished 새로운 게시 상태
     * @param modifiedByEmplId 수정자 교직원 ID (String 타입)
     * @return 업데이트된 게시글의 응답 DTO
     */
    @Transactional
    public PostResponse updatePostPublishStatus(Long postId, Boolean isPublished, String modifiedByEmplId) {
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + postId));
        
        existingPost.setIsPublished(isPublished);
        existingPost.setModifiedBy(modifiedByEmplId);
        existingPost.setModifiedAt(LocalDateTime.now());
        
        return PostResponse.from(existingPost);
    }

    /**
     * 대시보드에 표시할 공지사항 통계 데이터를 조회합니다.
     *
     * @return 공지사항 통계 응답 DTO
     */
    @Transactional(readOnly = true)
    public PostStatisticsResponse getPostStatistics() {
        long totalPosts = postRepository.count();
        long activePosts = postRepository.countByIsPublished(true);
        long importantPosts = postRepository.countByIsImportant(true);
        // ⭐ 평균 조회수 계산 로직 추가 ⭐
        double averageViewCount = postRepository.findAverageViewCount(); 
        long recent30DaysNewPosts = 0;
        double recent30DaysViewIncreaseRate = 0.0;

        return PostStatisticsResponse.builder()
                .totalPosts(totalPosts)
                .activePosts(activePosts)
                .importantPosts(importantPosts)
                .averageViewCount(averageViewCount)
                .recent30DaysNewPosts(recent30DaysNewPosts)
                .recent30DaysViewIncreaseRate(recent30DaysViewIncreaseRate)
                .build();
    }
    
    
}
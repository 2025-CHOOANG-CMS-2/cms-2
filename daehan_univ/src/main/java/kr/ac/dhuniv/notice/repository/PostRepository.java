package kr.ac.dhuniv.notice.repository;

import kr.ac.dhuniv.notice.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 조회 (기본 CRUD는 JpaRepository에서 제공)
    Optional<Post> findByPostId(Long postId);

    // 게시 상태별 조회
    Page<Post> findByIsPublished(Boolean isPublished, Pageable pageable);

    // 중요 공지 여부별 조회
    Page<Post> findByIsImportant(Boolean isImportant, Pageable pageable);

    // 제목 검색
    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // 게시판 ID별 조회
    Page<Post> findByBoardId(Long boardId, Pageable pageable);

    // 게시 상태와 중요 공지 여부 조합
    Page<Post> findByIsPublishedAndIsImportant(Boolean isPublished, Boolean isImportant, Pageable pageable);

    // 게시판 ID와 게시 상태 조합
    Page<Post> findByBoardIdAndIsPublished(Long boardId, Boolean isPublished, Pageable pageable);

    // 게시판 ID와 중요 공지 여부 조합
    Page<Post> findByBoardIdAndIsImportant(Long boardId, Boolean isImportant, Pageable pageable);

    // 게시판 ID, 게시 상태, 중요 공지 여부 조합
    Page<Post> findByBoardIdAndIsPublishedAndIsImportant(Long boardId, Boolean isPublished, Boolean isImportant, Pageable pageable);

    // 제목 검색과 게시판 ID 조합
    Page<Post> findByTitleContainingIgnoreCaseAndBoardId(String title, Long boardId, Pageable pageable);

    // 제목 검색과 게시 상태 조합
    Page<Post> findByTitleContainingIgnoreCaseAndIsPublished(String title, Boolean isPublished, Pageable pageable);

    // 제목 검색과 중요 공지 여부 조합
    Page<Post> findByTitleContainingIgnoreCaseAndIsImportant(String title, Boolean isImportant, Pageable pageable);

    // 제목 검색, 게시판 ID, 게시 상태 조합
    Page<Post> findByTitleContainingIgnoreCaseAndBoardIdAndIsPublished(String title, Long boardId, Boolean isPublished, Pageable pageable);

    // 제목 검색, 게시판 ID, 중요 공지 여부 조합
    Page<Post> findByTitleContainingIgnoreCaseAndBoardIdAndIsImportant(String title, Long boardId, Boolean isImportant, Pageable pageable);

    // 제목 검색, 게시 상태, 중요 공지 여부 조합
    Page<Post> findByTitleContainingIgnoreCaseAndIsPublishedAndIsImportant(String title, Boolean isPublished, Boolean isImportant, Pageable pageable);

    // 제목 검색, 게시판 ID, 게시 상태, 중요 공지 여부 조합
    Page<Post> findByTitleContainingIgnoreCaseAndBoardIdAndIsPublishedAndIsImportant(String title, Long boardId, Boolean isPublished, Boolean isImportant, Pageable pageable);

    // 통계 관련 메서드 (PostService에서 사용)
    long countByIsPublished(Boolean isPublished);
    long countByIsImportant(Boolean isImportant);
    long countByCreatedAtAfter(LocalDateTime dateTime);

    // ⭐ 평균 조회수를 계산하는 쿼리 메서드 추가 ⭐
    @Query("SELECT COALESCE(AVG(p.viewCount), 0) FROM Post p")
    double findAverageViewCount();

    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM Post p")
    long sumAllViewCounts();

    @Query("SELECT COALESCE(SUM(p.viewCount), 0) FROM Post p WHERE p.createdAt >= :dateTime")
    long sumViewCountForPostsCreatedAfter(LocalDateTime dateTime);
}

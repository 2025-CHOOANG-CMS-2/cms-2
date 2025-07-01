package kr.ac.dhuniv.notice.repository;

import kr.ac.dhuniv.notice.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID; // UUID 임포트 추가

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    // UUID로 첨부파일 조회
    Optional<Attachment> findByUuid(String uuid);

    // ⭐ Post 엔티티의 postId 필드를 기준으로 첨부파일 목록 조회 ⭐
    // Attachment 엔티티의 'post' 필드를 통해 'Post' 엔티티에 접근하고,
    // 'Post' 엔티티의 'postId' 필드를 사용합니다.
    List<Attachment> findByPost_PostId(Long postId); // ⭐ 이 부분을 수정합니다. ⭐
}

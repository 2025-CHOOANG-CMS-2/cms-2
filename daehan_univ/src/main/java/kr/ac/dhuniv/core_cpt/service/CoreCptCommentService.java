package kr.ac.dhuniv.core_cpt.service;

import jakarta.transaction.Transactional;
import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.dto.comment.CoreCptCommentRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.comment.CoreCptCommentResponseDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptCommentTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreCptCommentService {
    private final CoreCptCommentTemplateRepository commentRepo;
    private final CoreCptInfoRepository coreCptInfoRepository;
    public String getCommentByScore(Long cciId, int score) {
        return commentRepo.findByCoreCpt_CciIdAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(
                        cciId, score, score)
                .map(CoreCptCommentTemplate::getContent)
                .orElse("적절한 코멘트가 없습니다.");
    }

    /**
     * ✅ 점수 구간 코멘트 등록
     * @param cciId 상위 역량 ID
     * @param dto 점수 구간 + 코멘트 요청 DTO
     * @return 저장된 코멘트 엔티티
     */
    public CoreCptCommentTemplate registerComment(Long cciId, CoreCptCommentRequestDTO dto) {
        // (1) 상위 역량 엔티티 조회 (없으면 예외 발생)
        CoreCptInfo coreCpt = coreCptInfoRepository.findById(cciId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상위 역량 ID가 존재하지 않습니다: " + cciId));

        // (2) 엔티티 빌드
        CoreCptCommentTemplate entity = CoreCptCommentTemplate.builder()
                .coreCpt(coreCpt)                   // 상위 역량 연관
                .minScore(dto.getMinScore())        // 최소 점수
                .maxScore(dto.getMaxScore())        // 최대 점수
                .content(dto.getContent())          // 코멘트 내용
                .scoreLevel(dto.getScoreLevel())    // 점수 구간명 (선택적)
                .build();

        // (3) DB에 저장
        return commentRepo.save(entity);
    }

    /**
     * ✅ 상위 역량별 점수 구간 코멘트 목록 조회
     * @param cciId 상위 역량 ID
     * @return DTO 리스트
     */
    public List<CoreCptCommentResponseDTO> getComments(Long cciId) {
        // DB에서 코멘트 목록 조회
        List<CoreCptCommentTemplate> entities = commentRepo.findByCoreCpt_CciIdOrderByMinScoreAsc(cciId);

        // 엔티티 → DTO 변환
        return entities.stream()
                .map(entity -> CoreCptCommentResponseDTO.builder()
                        .id(entity.getCmt_id())
                        .minScore(entity.getMinScore())
                        .maxScore(entity.getMaxScore())
                        .content(entity.getContent())
                        //.scoreLevel(entity.getScoreLevel())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * ✅ 특정 상위 역량의 모든 점수 구간 코멘트를 조회
     * @param cciId 상위 역량 ID
     * @return DTO 리스트
     */
    public List<CoreCptCommentResponseDTO> getCommentsByCompetency(Long cciId) {
        List<CoreCptCommentTemplate> entities = commentRepo.findByCoreCpt_CciId(cciId);
        List<CoreCptCommentResponseDTO> dtos = new ArrayList<>();

        for (CoreCptCommentTemplate e : entities) {
            CoreCptCommentResponseDTO dto = new CoreCptCommentResponseDTO();
            dto.setId(e.getCmt_id());
            dto.setMinScore(e.getMinScore());
            dto.setMaxScore(e.getMaxScore());
            dto.setContent(e.getContent());
            dtos.add(dto);
        }

        return dtos;
    }
    public CoreCptCommentResponseDTO  updateComment(Long cciId, Long commentId, CoreCptCommentResponseDTO dto) {
        CoreCptCommentTemplate comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("코멘트를 찾을 수 없습니다: " + commentId));

        if (!comment.getCoreCpt().getCciId().equals(cciId)) {
            throw new IllegalArgumentException("코멘트가 해당 상위 역량에 속하지 않습니다.");
        }

        // 수정
        comment.setMinScore(dto.getMinScore());
        comment.setMaxScore(dto.getMaxScore());
        comment.setContent(dto.getContent());

        commentRepo.save(comment);

        return new CoreCptCommentResponseDTO().builder()
                .id(comment.getCmt_id())
                .minScore(comment.getMinScore())
                .maxScore(comment.getMaxScore())
                .content(comment.getContent())
                .build();
    }
    @Transactional
    public void deleteCommentById(Long commentId) {
        // 주어진 ID의 코멘트가 존재하는지 검증
        CoreCptCommentTemplate comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("코멘트를 찾을 수 없습니다. ID: " + commentId));

        // 삭제 실행
        commentRepo.delete(comment);
    }


}
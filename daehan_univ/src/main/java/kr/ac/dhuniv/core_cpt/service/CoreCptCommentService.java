package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.dto.comment.CoreCptCommentRequestDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptCommentTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
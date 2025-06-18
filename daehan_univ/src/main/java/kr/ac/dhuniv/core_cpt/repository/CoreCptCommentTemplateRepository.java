package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoreCptCommentTemplateRepository extends JpaRepository<CoreCptCommentTemplate, Long> {
    //특정 점수(score)가 구간에 속하는 코멘트 조회
    Optional<CoreCptCommentTemplate> findByCoreCpt_CciIdAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(
            Long cciId, Integer score1, Integer score2);
    /**
     * ✅ 특정 상위 역량에 등록된 코멘트 구간 전체 조회
     * @param cciId 상위 역량 ID
     * @return 코멘트 리스트
     */
    List<CoreCptCommentTemplate> findByCoreCpt_CciIdOrderByMinScoreAsc(Long cciId);
    // 특정 상위 역량의 코멘트 목록 조회
    List<CoreCptCommentTemplate> findByCoreCpt_CciId(Long cciId);
}
package kr.ac.dhuniv.core_cpt.repository;

import kr.ac.dhuniv.core_cpt.domain.CoreCptCommentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoreCptCommentTemplateRepository extends JpaRepository<CoreCptCommentTemplate, Long> {
    //특정 점수(score)가 구간에 속하는 코멘트 조회
    Optional<CoreCptCommentTemplate> findByCoreCpt_CciIdAndMinScoreLessThanEqualAndMaxScoreGreaterThanEqual(
            Long cciId, Integer score1, Integer score2);
}
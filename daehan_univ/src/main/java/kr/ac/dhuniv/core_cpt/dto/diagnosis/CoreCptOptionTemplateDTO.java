package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.*;

/**
 * ✅ CoreCptOptionTemplateDTO
 * - 선택지(option) 정보를 클라이언트로 전달하기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptOptionTemplateDTO {
    private Long optionId;     // 선택지 ID
    private String optionText; // 선택지 내용
    private Integer ord;       // 출력 순서
    private Integer score;     // 선택지 점수
}

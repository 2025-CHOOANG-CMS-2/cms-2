package kr.ac.dhuniv.counsel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WriteResultRequestDto {

    /**
     * 어떤 상담 신청에 대한 결과인지 식별하기 위한 ID
     * (cnsl_aply 테이블의 PK)
     */
    @NotNull(message = "상담 신청 ID가 없습니다.")
    private Long applyId;

    /**
     * 상담사가 작성하는 상담 내용
     */
    @NotBlank(message = "상담 내용을 입력해주세요.")
    private String counselingContent;

    /**
     * 학생이 평가할 만족도 점수
     * (결과 작성 시에는 상담사가 입력하지 않지만, 나중에 학생이 입력할 수 있도록 필드 포함)
     */
    @Min(value = 0, message = "만족도 점수는 0 이상이어야 합니다.")
    @Max(value = 5, message = "만족도 점수는 5 이하여야 합니다.")
    private Double satisfactionScore;
    
    // 이 외에 상담 결과 작성 시 필요한 정보가 있다면 여기에 필드를 추가합니다.
}
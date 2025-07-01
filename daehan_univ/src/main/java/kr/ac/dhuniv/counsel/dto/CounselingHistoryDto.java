package kr.ac.dhuniv.counsel.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data // Getter, Setter, ToString, EqualsAndHashCode, RequiredArgsConstructor 등을 모두 포함
public class CounselingHistoryDto {

    private Long applyId;             // 상담 신청 ID (PK)
    private String cnslAplyId;          // 상담 신청 비즈니스 ID
    
    private String counselorName;       // 상담사 이름
    private String counselingType;      // 상담 유형 (예: 진로 상담)
    private String counselingMethod;    // 상담 방식 (예: 화상 상담)
    private String status;              // 예약 상태 (예: PENDING, APPROVED, COMPLETED)
    private LocalDateTime applyDateTime;  // 예약된 일시
    private String content;             // 학생이 신청 시 작성한 내용

    // 아래는 상담 완료(COMPLETED) 시에만 값이 채워지는 필드들입니다.
    private Long resultId;              // 상담 결과 ID (PK)
    private String resultContent;         // 상담사가 작성한 결과 내용
    private Double satisfactionScore;     // 만족도
    private LocalDateTime counselingDateTime; // 실제 상담이 진행된 일시
}
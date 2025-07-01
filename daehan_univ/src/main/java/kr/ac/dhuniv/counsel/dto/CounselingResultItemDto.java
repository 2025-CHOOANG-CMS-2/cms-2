package kr.ac.dhuniv.counsel.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CounselingResultItemDto {
    // 목록 표시에 필요한 데이터들
    private Long applyId;             // 상담 신청 ID (결과 작성 시 필요)
    private Long resultId;            // 상담 결과 ID (결과 수정 시 필요)
    
    private String studentName;       // 학생 이름
    private String studentId;         // 학번
    private String studentMajor;      // 학생 전공
    
    private String counselingType;    // 상담 유형
    private String status;            // 상담 상태 (APPROVED, COMPLETED)
    private LocalDateTime applyDateTime; // 상담 신청(예약) 일시
    
    private String resultContent;       // 상담사가 작성한 결과 요약
    private Double satisfactionScore;   // 학생이 남긴 평점
}
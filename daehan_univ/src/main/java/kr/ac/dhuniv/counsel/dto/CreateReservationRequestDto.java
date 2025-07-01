package kr.ac.dhuniv.counsel.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateReservationRequestDto {

    /**
     * 예약을 신청한 학생의 학번
     * (로그인 기능 구현 전까지는 JS에서 임시로 하드코딩하여 전송)
     */
    @NotBlank(message = "학생 정보가 없습니다.")
    private String stdNo;

    /**
     * 예약 대상 상담사의 사번
     * (프론트엔드 '상담사 선택' 드롭다운에서 선택된 값)
     */
    @NotBlank(message = "상담사를 선택해주세요.")
    private String emplNo;
    
    /**
     * 상담 유형
     * (프론트엔드 '상담 유형 선택' 드롭다운에서 선택된 값)
     */
    @NotBlank(message = "상담 유형을 선택해주세요.")
    private String counselingType;

    /**
     * 예약하려는 날짜와 시간
     * (JS에서 "YYYY-MM-DD" 와 "HH:MM"을 조합하여 "YYYY-MM-DDTHH:MM:SS" 형식으로 전송)
     */
    @NotNull(message = "예약 일시를 선택해주세요.")
    @Future(message = "예약 시간은 현재 시간 이후여야 합니다.")
    private LocalDateTime applyDateTime;

    /**
     * 상담 방식
     * (폼에서 선택한 'online' 또는 'offline')
     */
    @NotBlank(message = "상담 방식을 선택해주세요.")
    private String counselingMethod;

    /**
     * 상담 신청 내용
     * (폼에 입력한 상세 내용)
     */
    @NotBlank(message = "상담 내용을 입력해주세요.")
    private String content;
    
    /**
     * 학생 연락처
     * (폼에 입력한 연락처)
     */
    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "^\\d{10,11}$", message = "올바른 형식의 연락처를 입력해주세요.")
    private String phone;
    
    private Long originalApplyId;
}
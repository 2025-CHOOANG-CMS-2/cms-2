package kr.ac.dhuniv.ncs.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import lombok.Data;
@Data
public class ProgramDto {
    private Long prgId;
    private String prgCode;
    private String prgNm;
    private String prgDesc;
    private Integer maxCnt;
    private LocalDateTime aplyBgngYmd;
    private LocalDateTime aplyEndYmd;
    private LocalDateTime prgStDt;
    private LocalDateTime prgEndDt;
    private String imageUrl;
    private LocalDateTime regDt;
    private LocalDateTime updDt;
    private String regUserId;
    private String updUserId;
    private String categoryName;
    private BigDecimal mileageScore;
    private Long cciId;
    private String mileageCode;
    /**
     * 상태 코드(upcoming, active, ended, draft 등)를 반환
     */
    public String getStatus() {
        LocalDateTime now = LocalDateTime.now();
        // 신청 시작 전
        if (aplyBgngYmd != null && now.isBefore(aplyBgngYmd)) {
            return "upcoming";
        }
        // 신청 가능 기간
        if (aplyBgngYmd != null && aplyEndYmd != null
            && !now.isBefore(aplyBgngYmd) && !now.isAfter(aplyEndYmd)) {
            return "active";
        }
        // 프로그램 종료 후
        if (prgEndDt != null && now.isAfter(prgEndDt)) {
            return "ended";
        }
        return "active";
    }

    /**
     * 화면에 보여줄 상태 텍스트를 반환
     */
    public String getStatusText() {
        switch (getStatus()) {
            case "upcoming": return "신청예정";
            case "active":   return "신청가능";
            case "ended":    return "종료";
            default:         return "";
        }
    }
    
    /**
     * 마일리지 소수점 이하 버리고 정수만 리턴
     */
    public Integer getMileageScoreInt() {
        if (mileageScore == null) {
            return null;
        }
        // 소수점 이하를 버리고 정수로 변환
        return mileageScore.setScale(0, RoundingMode.DOWN).intValue();
    }

    // (선택) 바로 "점"까지 붙여서 문자열로 리턴하고 싶으면
    public String getMileageScoreText() {
        Integer v = getMileageScoreInt();
        return v != null ? v + "점" : "";
    }
    
    public String getMileageCode() { return mileageCode; }
    public void setMileageCode(String mileageCode) { this.mileageCode = mileageCode; }
}

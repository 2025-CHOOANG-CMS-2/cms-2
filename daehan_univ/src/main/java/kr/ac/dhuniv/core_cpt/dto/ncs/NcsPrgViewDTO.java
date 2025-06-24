package kr.ac.dhuniv.core_cpt.dto.ncs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 추천 비교과 프로그램 조회용 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NcsPrgViewDTO {

    private Long prgId;                 // 프로그램 ID

    private String prgNm;               // 프로그램명

    private String prgDesc;             // 프로그램 설명

    private String coreCptName;         // 연관 핵심역량명

    private String coreCptColorHex;     // 핵심역량 색상 HEX

    private BigDecimal mileageScore;    // 마일리지 점수

    private int prgCapacity;            // 모집 인원

    private int appliedCount;           // 신청자 수

    private LocalDateTime prgEndDate;   // 모집 마감일자

}

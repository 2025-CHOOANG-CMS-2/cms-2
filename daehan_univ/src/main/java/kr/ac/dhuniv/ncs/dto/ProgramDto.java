package kr.ac.dhuniv.ncs.dto;

import java.math.BigDecimal;
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
}

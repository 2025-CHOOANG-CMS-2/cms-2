package kr.ac.dhuniv.ncs.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import kr.ac.dhuniv.ncs.dto.NcsApplicationDto;

@Data
public class NcsApplicationDto {
    // 신청 정보
    private Long aplyId;
    private LocalDateTime aplyDt;
    private String aplyStatCd; // APPLY, REJECT, CONFIRM

    // 프로그램 정보
    private Long prgId;
    private String prgNm;
    private String categoryName; // 카테고리명 (JOIN)
    private Integer maxCnt;      // 정원

    // 학생 정보
    private Long stdId;
    private String stdNo;        // 학번
    private String stdNm;        // 학생 이름
    private Integer schYr;       // 학년
}
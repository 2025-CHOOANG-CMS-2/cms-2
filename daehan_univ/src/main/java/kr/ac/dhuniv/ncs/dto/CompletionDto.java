package kr.ac.dhuniv.ncs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompletionDto {
    // 이수 정보
    private Long cmpId;
    private Long aplyId;
    private LocalDateTime cmpDt;
    private String cmpStatCd; // ING, CMP

    // 프로그램 정보
    private String prgNm;

    // 학생 정보
    private String stdNo;
    private String stdNm;
    private String deptNm;
    private Integer schYr;
}
package kr.ac.dhuniv.ncs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MyProgramDto {

    // ncs_prg_info 정보
    private Long prgId;
    private String prgNm;
    private LocalDateTime prgStDt;
    private LocalDateTime prgEndDt;

    // core_cpt_info 정보
    private String categoryName;

    // ncs_prg_aply 정보
    private Long aplyId;
    private LocalDateTime aplyDt;
    private String aplyStatCd; // 예: APLIED, CANCELED

    // ncs_cmp_info 정보
    private String cmpStatCd; // 예: CMP, NCP, ING

    // 화면 표시용 가공 필드
    private String overallStatus; // 최종 상태 (pending, ongoing, completed, rejected)
    private String overallStatusText; // 최종 상태 한글 텍스트
}
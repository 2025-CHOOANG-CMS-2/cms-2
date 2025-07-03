package kr.ac.dhuniv.ncs.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NcsPrgAplyDto {
    private Long aplyId;
    private Long prgId;
    private Long stdId;
    private LocalDateTime aplyDt;
    private String aplyStatCd;
    private String aplySelCd;
    private String aplyCode;
}
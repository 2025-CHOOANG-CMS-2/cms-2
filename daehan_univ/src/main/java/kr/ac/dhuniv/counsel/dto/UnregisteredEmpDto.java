package kr.ac.dhuniv.counsel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnregisteredEmpDto {
    private String emplNo;
    private String emplNm;
    private String emplEmailAddr;
    private String emplTelno;
}
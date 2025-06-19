package kr.ac.dhuniv.counsel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // JPQL의 new 생성자에서 사용하기 위함
public class CounselorSimpleDto {
    private String emplNo;
    private String emplNm;
}
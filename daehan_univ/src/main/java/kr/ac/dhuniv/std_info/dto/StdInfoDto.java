package kr.ac.dhuniv.std_info.dto;

import java.time.LocalDate; // LocalDateTime 대신 LocalDate 사용

import lombok.Data;
import lombok.NoArgsConstructor; // 기본 생성자 추가 (Lombok)
import lombok.AllArgsConstructor; // 모든 필드 생성자 추가 (Lombok)

@Data 
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드 포함 생성자
public class StdInfoDto {


    private String STD_NO;
    private String STD_NM;
    private String SCSBJT_CD;
    private Integer SCH_YR;
    private LocalDate ENTR_DT; // LocalDate로 변경
    private String STD_STAT_CD;
    private String STD_ZIP;
    private String STD_ADDR;
    private String STD_DADDR;
    private String STD_TELNO;
    private String STD_EML_ADDR;
    private Long USER_ID2; 
    private String USE_YN;
}
package kr.ac.dhuniv.std_info.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty; // 이 import 추가
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StdInfoDto {

	@JsonProperty("STD_ID")
    private Long STD_ID;
    @JsonProperty("STD_NO")
    private String STD_NO;
    @JsonProperty("STD_NM")
    private String STD_NM;
    @JsonProperty("SCSBJT_CD")
    private String SCSBJT_CD;
    @JsonProperty("SCH_YR")
    private Integer SCH_YR;
    @JsonProperty("ENTR_DT")
    private LocalDate ENTR_DT;
    @JsonProperty("STD_STAT_CD")
    private String STD_STAT_CD;
    @JsonProperty("STD_ZIP")
    private String STD_ZIP;
    @JsonProperty("STD_ADDR")
    private String STD_ADDR;
    @JsonProperty("STD_DADDR")
    private String STD_DADDR;
    @JsonProperty("STD_TELNO")
    private String STD_TELNO;
    @JsonProperty("STD_EML_ADDR")
    private String STD_EML_ADDR;
    @JsonProperty("USER_ID2")
    private String USER_ID2;
    @JsonProperty("USE_YN")
    private String USE_YN;
   
}
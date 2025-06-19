package kr.ac.dhuniv.std_info.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String STD_NO; // 학번 (User.userId와 연결)
    @JsonProperty("STD_NM")
    private String STD_NM;
    @JsonProperty("SCSBJT_CD")
    private String SCSBJT_CD;
    @JsonProperty("SCH_YR")
    private Integer SCH_YR; // 학년 (StdInfo.schoolYear 매핑)
    @JsonProperty("ENTR_DT")
    private LocalDate ENTR_DT; // 입학일자 (StdInfo.entranceDate 매핑)
    @JsonProperty("STD_STAT_CD") // 재학 상태 코드 (StdInfo.statusCode 매핑)
    private String STD_STAT_CD;
    @JsonProperty("STD_ZIP")
    private String STD_ZIP; // 우편번호 (StdInfo.stdZip 매핑)
    @JsonProperty("STD_ADDR")
    private String STD_ADDR; // 주소 (StdInfo.stdAddr 매핑)
    @JsonProperty("STD_DADDR")
    private String STD_DADDR; // 상세 주소 (StdInfo.stdDaddr 매핑)
    @JsonProperty("STD_TELNO")
    private String STD_TELNO; // 전화번호 (StdInfo.stdTelno 매핑)
    @JsonProperty("STD_EML_ADDR")
    private String STD_EML_ADDR; // 이메일 (StdInfo.stdEmlAddr 매핑)
    @JsonProperty("CREATED_BY") // 등록 관리자 ID (StdInfo.createdBy 매핑)
    private String CREATED_BY;
    @JsonProperty("USE_YN") // 사용 여부 (StdInfo.useYn 매핑)
    private String USE_YN;
    @JsonProperty("PROFILE_IMAGE_URL") // 프로필 이미지 URL (StdInfo.profileImageUrl 매핑)
    private String PROFILE_IMAGE_URL;
    
}

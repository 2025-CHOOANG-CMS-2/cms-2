package kr.ac.dhuniv.empl_info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // ⭐ 이 import 문을 추가합니다 ⭐

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // ⭐ 이 어노테이션을 추가합니다 ⭐
public class EmplInfoDto {

    @JsonProperty("STAFF_NO")
    private String STAFF_NO;

    @JsonProperty("STAFF_NM")
    private String STAFF_NM;

    @JsonProperty("DEPT_CD")
    private String DEPT_CD;

    @JsonProperty("POSITION_CD")
    private String POSITION_CD;

    @JsonProperty("STATUS_CD")
    private String STATUS_CD;

    @JsonProperty("HIRE_DT")
    private LocalDate HIRE_DT;

    @JsonProperty("ZIP_CD")
    private String ZIP_CD;

    @JsonProperty("ADDR")
    private String ADDR;

    @JsonProperty("DADDR")
    private String DADDR;
    
    @JsonProperty("DEPT_NM")
    private String DEPT_NM; // 부서 이름 필드

    @JsonProperty("STAFF_TELNO")
    private String STAFF_TELNO;

    @JsonProperty("STAFF_EML_ADDR")
    private String STAFF_EML_ADDR;

    @JsonProperty("USE_YN")
    private String USE_YN;

    @JsonProperty("CREATED_BY")
    private String CREATED_BY;

    @JsonProperty("PROFILE_IMAGE_URL")
    private String PROFILE_IMAGE_URL;
}
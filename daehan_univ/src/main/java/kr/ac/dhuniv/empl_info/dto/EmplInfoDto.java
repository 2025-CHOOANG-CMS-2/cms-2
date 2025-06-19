package kr.ac.dhuniv.empl_info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmplInfoDto {

    // @JsonProperty("EMPL_ID") // 필요하다면 ID도 추가할 수 있습니다.
    // private Long EMPL_ID; // 엔티티의 ID와 매핑될 수 있음

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

    @JsonProperty("STAFF_TELNO")
    private String STAFF_TELNO;

    @JsonProperty("STAFF_EML_ADDR")
    private String STAFF_EML_ADDR;

    @JsonProperty("USE_YN")
    private String USE_YN;

    @JsonProperty("CREATED_BY")
    private String CREATED_BY;

    // --- 새로 추가되는 DTO 필드 ---
    @JsonProperty("PROFILE_IMAGE_URL")
    private String PROFILE_IMAGE_URL;
}
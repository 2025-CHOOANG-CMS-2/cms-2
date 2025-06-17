package kr.ac.dhuniv.empl_info.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate; // 입사일자(HIRE_DT)가 날짜 타입이라면 필요

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자
public class EmplInfoDto {


    @JsonProperty("STAFF_NO")
    private String STAFF_NO;

    @JsonProperty("STAFF_NM")
    private String STAFF_NM;


    @JsonProperty("DEPT_CD")
    private String DEPT_CD;

    @JsonProperty("POSITION_CD")
    private String POSITION_CD;

    // 프론트엔드 STATUS_CD (재직상태 코드) -> 엔티티 emplStatCd
    @JsonProperty("STATUS_CD")
    private String STATUS_CD;


    @JsonProperty("HIRE_DT")
    private LocalDate HIRE_DT;

    // 프론트엔드 ZIP_CD (우편번호) -> 엔티티 emplZip
    @JsonProperty("ZIP_CD")
    private String ZIP_CD;

    // 프론트엔드 ADDR (주소) -> 엔티티 emplAddr
    @JsonProperty("ADDR")
    private String ADDR;

    // 프론트엔드 DADDR (상세주소) -> 엔티티 emplDaddr
    @JsonProperty("DADDR")
    private String DADDR;

    // 프론트엔드 STAFF_TELNO (연락처) -> 엔티티 emplTelno
    @JsonProperty("STAFF_TELNO")
    private String STAFF_TELNO;

    // 프론트엔드 STAFF_EML_ADDR (이메일) -> 엔티티 emplEmailAddr
    @JsonProperty("STAFF_EML_ADDR")
    private String STAFF_EML_ADDR;

    // 프론트엔드 USE_YN (사용 여부) -> 엔티티 useYn
    @JsonProperty("USE_YN")
    private String USE_YN;

    // 프론트엔드 USER_ID2 (등록/수정 관리자) -> 엔티티 User의 userId
    @JsonProperty("USER_ID2")
    private String USER_ID2;
    

}

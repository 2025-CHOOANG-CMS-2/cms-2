package kr.ac.dhuniv.mileage.dto;

import java.time.LocalDateTime;

import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CmpInfoResponseDto {
	private Long cmpId;
    private Long stdId;
    private String prgName;
    private LocalDateTime completeDate;
    private String statusCode;

    public static CmpInfoResponseDto fromEntity(NcsCmpInfo cmpInfo) {
        return CmpInfoResponseDto.builder()
            .cmpId(cmpInfo.getCmpId())
            .stdId(cmpInfo.getStudent().getStdId())
            .prgName(cmpInfo.getProgram().getPrgNm()) // getPrgNm() 는 NcsPrgInfo에 필요
            .completeDate(cmpInfo.getCompleteDate())
            .statusCode(cmpInfo.getStatusCode())
            .build();
    }
}

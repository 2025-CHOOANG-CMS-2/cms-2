package kr.ac.dhuniv.counsel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationStatusDto {

    /**
     * 변경하고자 하는 새로운 상태 코드
     * 예: "CANCELED", "APPROVED", "REJECTED"
     */
    @NotBlank(message = "변경할 상태 코드가 없습니다.")
    private String status;
}
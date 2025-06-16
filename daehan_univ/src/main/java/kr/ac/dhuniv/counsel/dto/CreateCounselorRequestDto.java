package kr.ac.dhuniv.counsel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateCounselorRequestDto {
    private String emplNo;
    private String cnslSpec;
    private String intro;
    private Boolean isActive;
}
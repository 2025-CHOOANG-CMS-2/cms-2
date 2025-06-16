package kr.ac.dhuniv.counsel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCounselorStatusDto {
    private Boolean isActive;
}
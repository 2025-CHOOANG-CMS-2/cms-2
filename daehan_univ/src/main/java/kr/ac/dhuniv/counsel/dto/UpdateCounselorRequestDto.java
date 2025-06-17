package kr.ac.dhuniv.counsel.dto;

import lombok.Data;

@Data
public class UpdateCounselorRequestDto {
    private String cnslSpec;
    private String intro;
    private Boolean isActive;
}
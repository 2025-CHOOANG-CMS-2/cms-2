package kr.ac.dhuniv.counsel.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AvailableSlotsDto {
    // 특정 날짜에 예약 가능한 상담사 한 명의 정보를 담는 내부 클래스
	private String counselorEmplNo;
    private String counselorName;
    private String time;
}
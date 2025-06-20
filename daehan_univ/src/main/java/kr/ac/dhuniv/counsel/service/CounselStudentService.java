/*
package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.dto.AvailableSlotsDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CounselStudentService {

    */
/**
     * 상담 유형으로 상담사 목록을 조회합니다.
     * @param counselingType 상담 유형
     * @return 상담사 정보 DTO 리스트
     *//*

    List<CounselorSimpleDto> findCounselorsByType(String counselingType);

    */
/**
     * 특정 연월과 필터 조건에 맞는 모든 날짜의 예약 가능 시간 슬롯 정보를 조회합니다.
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param counselingType 상담 유형 필터
     * @param counselorId 특정 상담사 필터
     * @return Key: "YYYY-MM-DD", Value: 해당 날짜에 예약 가능한 시간 정보 리스트
     *//*

    Map<String, List<AvailableSlotsDto>> getAvailableSlotsForMonth(Integer year, Integer month, String counselingType, String counselorId);
    
    // 여기에 나중에 상담 예약을 생성하는 createReservation 메소드도 추가할 것입니다.
}*/

package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.dto.CounselorScheduleDto;
import kr.ac.dhuniv.counsel.dto.DefaultScheduleDto;
import kr.ac.dhuniv.counsel.dto.ScheduleExceptionDto;

import java.time.LocalDate;
import java.util.List;

/**
 * 상담사의 근무 시간(기본, 예외)을 관리하는 서비스의 인터페이스입니다.
 */
public interface CounselorScheduleService {

    /**
     * 특정 상담사의 전체 스케줄(기본+예외) 정보를 조회합니다.
     * 프론트엔드 탭 로딩 시, 기존 설정 값을 보여주기 위해 사용됩니다.
     *
     * @param counselorId 조회할 상담사의 ID
     * @param year 조회할 연도 (예외 스케줄 조회용)
     * @param month 조회할 월 (예외 스케줄 조회용)
     * @return DTO로 변환된 전체 스케줄 정보
     */
    CounselorScheduleDto getCounselorSchedule(String counselorId, int year, int month);
    
    /**
     * '기본 근무시간 저장' 버튼에 연결될 기능입니다.
     * 7일치 요일별 기본 근무시간을 일괄적으로 업데이트합니다.
     *
     * @param counselorId 대상 상담사 ID
     * @param scheduleDtos 프론트엔드에서 보낸 7일치 근무시간 정보 DTO 리스트
     */
    void updateDefaultSchedules(String counselorId, List<DefaultScheduleDto> scheduleDtos);

    /**
     * '예외 설정 저장' 버튼에 연결될 기능입니다.
     * 특정 날짜의 스케줄을 새로 저장하거나 이미 존재하면 수정합니다.
     *
     * @param counselorId 대상 상담사 ID
     * @param exceptionDto 프론트엔드에서 보낸 특정일 예외 정보 DTO
     */
    void saveOrUpdateScheduleException(String counselorId, ScheduleExceptionDto exceptionDto);

    /**
     * '예외 설정 삭제' 버튼에 연결될 기능입니다.
     * 특정 날짜에 설정된 예외 스케줄을 삭제합니다.
     *
     * @param counselorId 대상 상담사 ID
     * @param date 삭제할 날짜
     */
    void deleteScheduleException(String counselorId, LocalDate date);
}

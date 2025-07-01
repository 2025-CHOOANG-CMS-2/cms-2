package kr.ac.dhuniv.counsel.controller;

import kr.ac.dhuniv.counsel.dto.CounselorScheduleDto;
import kr.ac.dhuniv.counsel.dto.DefaultScheduleDto;
import kr.ac.dhuniv.counsel.dto.ScheduleExceptionDto;
import kr.ac.dhuniv.counsel.service.CounselorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/counselor/schedule") // 스케줄 관리 전용 API 경로
@RequiredArgsConstructor
public class CounselorScheduleApiController {

    // 이전에 생성한 서비스 계층을 주입받습니다.
    private final CounselorScheduleService scheduleService;

    /**
     * [GET] 현재 설정된 스케줄 정보 조회 API
     * 역할: 프론트엔드에서 '기본 근무시간 설정' 탭을 처음 열 때,
     * DB에 저장된 값을 가져와 화면에 표시하기 위해 호출됩니다.
     */
    @GetMapping
    public ResponseEntity<CounselorScheduleDto> getMySchedule(
            @RequestParam("year") int year, 
            @RequestParam("month") int month) {
        
        // TODO: 로그인 기능 구현 후, 실제 로그인한 상담사의 ID로 교체해야 합니다.
        String counselorId = "STAFF001"; 
        
        CounselorScheduleDto schedule = scheduleService.getCounselorSchedule(counselorId, year, month);
        return ResponseEntity.ok(schedule);
    }

    /**
     * [PUT] 기본 근무시간 일괄 저장 API
     * 역할: '기본 일정 저장' 버튼 클릭 시, 7일치 요일별 근무시간 전체를
     * 한 번에 업데이트하기 위해 호출됩니다.
     */
    @PutMapping("/default")
    public ResponseEntity<Void> updateDefaultSchedules(@RequestBody List<DefaultScheduleDto> scheduleDtos) {
        String counselorId = "2025110002"; // TODO: 실제 로그인한 상담사 ID로 교체
        scheduleService.updateDefaultSchedules(counselorId, scheduleDtos);
        return ResponseEntity.ok().build();
    }

    /**
     * [POST] 특정일 예외 설정 저장/수정 API
     * 역할: '예외 설정 저장' 버튼 클릭 시, 특정 날짜의 스케줄을
     * 새로 저장(INSERT)하거나 이미 존재하면 수정(UPDATE)하기 위해 호출됩니다.
     */
    @PostMapping("/exception")
    public ResponseEntity<Void> saveOrUpdateException(@RequestBody ScheduleExceptionDto exceptionDto) {
        String counselorId = "2025110002"; // TODO: 실제 로그인한 상담사 ID로 교체
        scheduleService.saveOrUpdateScheduleException(counselorId, exceptionDto);
        return ResponseEntity.ok().build();
    }

    /**
     * [DELETE] 특정일 예외 설정 삭제 API
     * 역할: '예외 설정 삭제' 버튼 클릭 시, 특정 날짜에 설정된
     * 예외 스케줄을 삭제하기 위해 호출됩니다.
     */
    @DeleteMapping("/exception/{date}")
    public ResponseEntity<Void> deleteException(
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String counselorId = "2025110002"; // TODO: 실제 로그인한 상담사 ID로 교체
        scheduleService.deleteScheduleException(counselorId, date);
        return ResponseEntity.ok().build();
    }
}

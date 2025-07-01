package kr.ac.dhuniv.mileage.api_controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.dhuniv.mileage.dto.student.CompetencyGroupedMileageDTO;
import kr.ac.dhuniv.mileage.dto.student.MileageHistoryDTO;
import kr.ac.dhuniv.mileage.dto.student.MileageOverviewDTO;
import kr.ac.dhuniv.mileage.service.StudentMileageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/student/mileage")
@RequiredArgsConstructor
public class StudentMileageApiController {
	
    private final StudentMileageService mileageService;

    // 해당학생 마일리지 현황 API
    @GetMapping("/overview/{userId}")
    public ResponseEntity<MileageOverviewDTO> getOverview(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(mileageService.getOverview(userId));
    }
    
    // 해당학생 마일리지 지급내역 API
    @GetMapping("/history")
    @ResponseBody
    public Page<MileageHistoryDTO> historyApi(
      @RequestParam(name="userId", defaultValue = "2025004001") String userId,
      @RequestParam(name="startDate", required = false) LocalDate startDate,
      @RequestParam(name="endDate", required = false) LocalDate endDate,
      @RequestParam(name="competencyId", required = false) Long competencyId,
      @RequestParam(name="sort", defaultValue = "latest") String sort,
      @RequestParam(name="page", defaultValue = "0") int page,
      @RequestParam(name="size", defaultValue = "1") int size
    ) {
    	// LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;  //00:00:00
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;  //23:59:59.999999999
        
        Pageable pageable = PageRequest.of(page, size);
        System.out.println(mileageService.getHistory(userId, startDateTime, endDateTime, competencyId, sort, pageable));
        return mileageService.getHistory(userId, startDateTime, endDateTime, competencyId, sort, pageable);
    }
    
    // 핵심역량별 비교과 프로그램 마일리지 배점표
    @GetMapping("/mileage-points")
    public ResponseEntity<List<CompetencyGroupedMileageDTO>> getMileagePrograms() {
        return ResponseEntity.ok(mileageService.getAllGroupedMileagePrograms());
    }

}

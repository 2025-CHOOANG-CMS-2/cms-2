package kr.ac.dhuniv.mileage.api_controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

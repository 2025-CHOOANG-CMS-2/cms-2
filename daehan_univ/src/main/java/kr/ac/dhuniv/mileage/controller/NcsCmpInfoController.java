package kr.ac.dhuniv.mileage.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.dhuniv.mileage.dto.CmpInfoResponseDto;
import kr.ac.dhuniv.mileage.service.NcsCmpInfoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ncs/cmp")
public class NcsCmpInfoController {

	private final NcsCmpInfoService cmpInfoService;

    @GetMapping("/student/{stdNo}")
    public List<CmpInfoResponseDto> getCompletedPrograms(@PathVariable("stdId") Long stdId) {
        return cmpInfoService.getCompletedProgramsByStudent(stdId);
    }
}

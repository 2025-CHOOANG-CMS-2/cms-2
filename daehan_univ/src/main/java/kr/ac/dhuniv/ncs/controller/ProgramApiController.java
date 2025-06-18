package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.domain.ProgramListView;
import kr.ac.dhuniv.ncs.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/programs")
public class ProgramApiController {

    private final ProgramService programService;

    /**
     * 비교과 프로그램 목록 조회 (교직원용)
     */
    @GetMapping
    public List<ProgramListView> getProgramList() {
        return programService.getAllPrograms();
    }
}

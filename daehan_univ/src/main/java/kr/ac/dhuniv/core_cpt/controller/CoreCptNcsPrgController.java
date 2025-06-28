package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.ncs.NcsPrgViewDTO;
import kr.ac.dhuniv.core_cpt.service.CoreCptNcsPrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 추천 비교과 프로그램 관련 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
public class CoreCptNcsPrgController {
    private final CoreCptNcsPrgService coreCptNcsPrgService;
    /**
     * 추천 비교과 프로그램 목록을 조회하는 API
     *
     * @return 추천 비교과 프로그램 목록 (JSON)
     */
    @GetMapping("/api/programs/recommend")
    public List<NcsPrgViewDTO> getRecommendedPrograms() {
        // 서비스 레이어에서 추천 비교과 프로그램 DTO 리스트를 조회하여 반환
        return coreCptNcsPrgService.getRecommendedPrograms();
    }

}

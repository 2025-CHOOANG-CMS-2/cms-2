package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;

import kr.ac.dhuniv.core_cpt.repository.CoreCptOptionTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ 선택지 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class CoreCptOptionController {

    private final CoreCptOptionTemplateRepository optionRepository;

    /**
     * ✅ 리커트 척도 선택지 리스트 반환
     */
    @GetMapping("/likert")
    public ResponseEntity<List<CoreCptOptionTemplate>> getLikertOptions() {
        List<CoreCptOptionTemplate> options = optionRepository.findAllByOrderByOrdAsc();
        return ResponseEntity.ok(options);
    }
}
package kr.ac.dhuniv.ncs.controller;

import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.service.NcsPrgInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class NcsPrgInfoController {

    private final NcsPrgInfoService service;

    /**
     * 프로그램 목록 조회 (페이징, 검색 키워드, 카테고리 필터)
     */
    @GetMapping
    public ResponseEntity<Page<ProgramDto>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page",       defaultValue = "0") int page,
            @RequestParam(value = "size",       defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NcsPrgInfo> programs = service.getPrograms(keyword, categoryId, pageable);

        // Entity → DTO 매핑
        Page<ProgramDto> dtoPage = programs.map(p -> {
            ProgramDto dto = new ProgramDto();
            dto.setPrgId(p.getPrgId());
            dto.setPrgCode(p.getPrgCode());
            dto.setPrgNm(p.getPrgNm());
            dto.setPrgDesc(p.getPrgDesc());
            dto.setMaxCnt(p.getMaxCnt());
            dto.setAplyBgngYmd(p.getAplyBgngYmd());
            dto.setAplyEndYmd(p.getAplyEndYmd());
            dto.setPrgStDt(p.getPrgStDt());
            dto.setPrgEndDt(p.getPrgEndDt());
            dto.setImageUrl(p.getImageUrl());
            dto.setRegDt(p.getRegDt());
            dto.setUpdDt(p.getUpdDt());
            dto.setRegUserId(p.getRegUserId());
            dto.setUpdUserId(p.getUpdUserId());

            if (p.getCoreCpt() != null) {
                dto.setCategoryName(p.getCoreCpt().getCciNm());
            }
            
            // 마일리지 점수 매핑 추가
            if (p.getMileage() != null) {
                dto.setMileageScore(p.getMileage().getMileageScore());
            }
            
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 단건 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProgramDto> get(@PathVariable Long id) {
        NcsPrgInfo p = service.getProgram(id);
        ProgramDto dto = new ProgramDto();
        dto.setPrgId       (p.getPrgId());
        dto.setPrgCode     (p.getPrgCode());
        dto.setPrgNm       (p.getPrgNm());
        dto.setPrgDesc     (p.getPrgDesc());
        dto.setMaxCnt      (p.getMaxCnt());
        dto.setAplyBgngYmd (p.getAplyBgngYmd());
        dto.setAplyEndYmd  (p.getAplyEndYmd());
        dto.setPrgStDt     (p.getPrgStDt());
        dto.setPrgEndDt    (p.getPrgEndDt());
        dto.setImageUrl    (p.getImageUrl());
        dto.setRegDt       (p.getRegDt());
        dto.setUpdDt       (p.getUpdDt());
        dto.setRegUserId   (p.getRegUserId());
        dto.setUpdUserId   (p.getUpdUserId());
        if (p.getCoreCpt() != null) {
            dto.setCategoryName(p.getCoreCpt().getCciNm());
        }
        return ResponseEntity.ok(dto);
    }

    /**
     * 생성
     */
    @PostMapping
    public ResponseEntity<ProgramDto> create(@RequestBody ProgramDto dto) {
        NcsPrgInfo toSave = new NcsPrgInfo();
        // 필요한 필드만 Entity에 복사
        toSave.setPrgCode     (dto.getPrgCode());
        toSave.setPrgNm       (dto.getPrgNm());
        toSave.setPrgDesc     (dto.getPrgDesc());
        toSave.setMaxCnt      (dto.getMaxCnt());
        toSave.setAplyBgngYmd (dto.getAplyBgngYmd());
        toSave.setAplyEndYmd  (dto.getAplyEndYmd());
        toSave.setPrgStDt     (dto.getPrgStDt());
        toSave.setPrgEndDt    (dto.getPrgEndDt());
        toSave.setImageUrl    (dto.getImageUrl());
        // (categoryName → coreCpt 설정은 서비스 레이어에서 해주세요)
        NcsPrgInfo created = service.createProgram(toSave);

        // 생성된 Entity → DTO
        ProgramDto resp = new ProgramDto();
        resp.setPrgId       (created.getPrgId());
        resp.setPrgCode     (created.getPrgCode());
        resp.setPrgNm       (created.getPrgNm());
        resp.setPrgDesc     (created.getPrgDesc());
        resp.setMaxCnt      (created.getMaxCnt());
        resp.setAplyBgngYmd (created.getAplyBgngYmd());
        resp.setAplyEndYmd  (created.getAplyEndYmd());
        resp.setPrgStDt     (created.getPrgStDt());
        resp.setPrgEndDt    (created.getPrgEndDt());
        resp.setImageUrl    (created.getImageUrl());
        resp.setRegDt       (created.getRegDt());
        resp.setUpdDt       (created.getUpdDt());
        resp.setRegUserId   (created.getRegUserId());
        resp.setUpdUserId   (created.getUpdUserId());
        if (created.getCoreCpt() != null) {
            resp.setCategoryName(created.getCoreCpt().getCciNm());
        }

        return ResponseEntity
                .created(URI.create("/api/programs/" + resp.getPrgId()))
                .body(resp);
    }

    /**
     * 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProgramDto> update(
            @PathVariable Long id,
            @RequestBody ProgramDto dto
    ) {
        NcsPrgInfo toUpdate = new NcsPrgInfo();
        toUpdate.setPrgNm       (dto.getPrgNm());
        toUpdate.setPrgDesc     (dto.getPrgDesc());
        toUpdate.setMaxCnt      (dto.getMaxCnt());
        toUpdate.setAplyBgngYmd (dto.getAplyBgngYmd());
        toUpdate.setAplyEndYmd  (dto.getAplyEndYmd());
        toUpdate.setPrgStDt     (dto.getPrgStDt());
        toUpdate.setPrgEndDt    (dto.getPrgEndDt());
        toUpdate.setImageUrl    (dto.getImageUrl());
        // (categoryName → coreCpt 설정은 서비스 레이어에서 해주세요)

        NcsPrgInfo updated = service.updateProgram(id, toUpdate);

        ProgramDto resp = new ProgramDto();
        resp.setPrgId       (updated.getPrgId());
        resp.setPrgCode     (updated.getPrgCode());
        resp.setPrgNm       (updated.getPrgNm());
        resp.setPrgDesc     (updated.getPrgDesc());
        resp.setMaxCnt      (updated.getMaxCnt());
        resp.setAplyBgngYmd (updated.getAplyBgngYmd());
        resp.setAplyEndYmd  (updated.getAplyEndYmd());
        resp.setPrgStDt     (updated.getPrgStDt());
        resp.setPrgEndDt    (updated.getPrgEndDt());
        resp.setImageUrl    (updated.getImageUrl());
        resp.setRegDt       (updated.getRegDt());
        resp.setUpdDt       (updated.getUpdDt());
        resp.setRegUserId   (updated.getRegUserId());
        resp.setUpdUserId   (updated.getUpdUserId());
        if (updated.getCoreCpt() != null) {
            resp.setCategoryName(updated.getCoreCpt().getCciNm());
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }
}

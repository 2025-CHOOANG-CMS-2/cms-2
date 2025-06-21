package kr.ac.dhuniv.ncs.service;

import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import kr.ac.dhuniv.ncs.repository.NcsPrgInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NcsPrgInfoServiceImpl implements NcsPrgInfoService {
    private final NcsPrgInfoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<NcsPrgInfo> getPrograms(String keyword, Long categoryId, Pageable pageable) {
        log.debug("getPrograms 호출: keyword={}, categoryId={}, pageable={}", keyword, categoryId, pageable);
        Page<NcsPrgInfo> result;
        if (keyword != null && !keyword.isEmpty()) {
            result = repository.findByPrgNmContainingIgnoreCase(keyword, pageable);
        } else if (categoryId != null) {
            result = repository.findByCoreCpt_CciId(categoryId, pageable);
        } else {
            result = repository.findAll(pageable);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public NcsPrgInfo getProgram(Long id) {
        NcsPrgInfo program = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("프로그램을 찾을 수 없습니다: " + id));
        return program;
    }

    @Override
    public NcsPrgInfo createProgram(NcsPrgInfo program) {
        LocalDateTime now = LocalDateTime.now();
        program.setRegDt(now);
        program.setUpdDt(now);
        log.debug("createProgram 요청 데이터: {}", program);
        NcsPrgInfo saved = repository.save(program);
        return saved;
    }

    @Override
    public NcsPrgInfo updateProgram(Long id, NcsPrgInfo program) {
        NcsPrgInfo existing = getProgram(id);
        existing.setPrgNm(program.getPrgNm());
        existing.setPrgDesc(program.getPrgDesc());
        existing.setCoreCpt(program.getCoreCpt());
        existing.setMaxCnt(program.getMaxCnt());
        existing.setAplyBgngYmd(program.getAplyBgngYmd());
        existing.setAplyEndYmd(program.getAplyEndYmd());
        existing.setPrgStDt(program.getPrgStDt());
        existing.setPrgEndDt(program.getPrgEndDt());
        existing.setImageUrl(program.getImageUrl());
        existing.setUpdDt(LocalDateTime.now());
        existing.setUpdUserId(program.getUpdUserId());
        NcsPrgInfo updated = repository.save(existing);
        return updated;
    }

    @Override
    public void deleteProgram(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("삭제할 프로그램을 찾을 수 없습니다: " + id);
        }
        repository.deleteById(id);
    }
}

package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptInfoDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptOptionTemplateDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptQstDTO;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstListDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptOptionTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * ✅ DiagnosisService
 * - 핵심역량 + 문항 + 선택지 데이터를 조회해 클라이언트에 제공하는 서비스
 */
@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final CoreCptInfoRepository coreCptInfoRepository;
    private final CoreCptOptionTemplateRepository coreCptOptionTemplateRepository;

    /**
     * ✅ 전체 상위 역량 트리 조회 (루트부터)
     */
    @Transactional(readOnly = true)
    public List<CoreCptInfoDTO> getAllCompetencyTree() {
        List<CoreCptInfo> roots = coreCptInfoRepository.findByParentIsNullOrderByCciIdAsc();
        List<CoreCptOptionTemplate> options = coreCptOptionTemplateRepository.findAll();
        return roots.stream()
                .map(root -> mapToDto(root, options))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 특정 상위 역량 ID 기준 트리 조회
     */
    @Transactional(readOnly = true)
    public CoreCptInfoDTO getCompetencyTreeByCciId(Long cciId) {
        CoreCptInfo entity = coreCptInfoRepository.findById(cciId)
                .orElseThrow(() -> new NoSuchElementException("상위역량 없음: " + cciId));
        List<CoreCptOptionTemplate> options = coreCptOptionTemplateRepository.findAll();
        return mapToDto(entity, options);
    }

    /**
     * ✅ CoreCptInfo -> CoreCptInfoDTO 재귀 매핑
     */
    public CoreCptInfoDTO mapToDto(CoreCptInfo entity, List<CoreCptOptionTemplate> options) {
        CoreCptInfoDTO dto = new CoreCptInfoDTO();
        dto.setCciId(entity.getCciId());
        dto.setCciCode(entity.getCciCode());
        dto.setCciNm(entity.getCciNm());
        dto.setCciDesc(entity.getCciDesc());
        dto.setColorHex(entity.getColorHex());

        // 문항 매핑
        dto.setQuestions(
                entity.getQuestions().stream().map(q -> {
                    CoreCptQstListDTO qdto = new CoreCptQstListDTO();
                    qdto.setQstId(q.getQstId());
                    qdto.setQstCode(q.getQstCode());
                    qdto.setQuestionText(q.getQstCont());
                    qdto.setQstOrd(q.getQstOrd());
                    qdto.setCompetencyName(entity.getCciNm());
                    qdto.setColorHex(entity.getColorHex());
                    qdto.setTopCompetencyName(entity.getParent() != null ? entity.getParent().getCciNm() : entity.getCciNm());
                    qdto.setSubCompetencyName(entity.getParent() != null ? entity.getCciNm() : null);
                    qdto.setTopCompetencyId(entity.getParent() != null ? entity.getParent().getCciId() : entity.getCciId());
                    qdto.setSubCompetencyId(entity.getParent() != null ? entity.getCciId() : null);

                    qdto.setOptions(
                            options.stream().map(opt -> CoreCptOptionTemplateDTO.builder()
                                            .optionId(opt.getOptionId())
                                            .optionText(opt.getOptionText())
                                            .ord(opt.getOrd())
                                            .score(opt.getScore())
                                            .build())
                                    .collect(Collectors.toList())
                    );
                    return qdto;
                }).collect(Collectors.toList())
        );

        // 하위 역량 재귀 매핑
        dto.setChildren(
                entity.getChildren().stream()
                        .map(child -> mapToDto(child, options))
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
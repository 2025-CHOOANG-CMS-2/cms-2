package kr.ac.dhuniv.core_cpt.service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;
import kr.ac.dhuniv.core_cpt.domain.CoreCptQst;

import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstListDTO;
import kr.ac.dhuniv.core_cpt.dto.qst.CoreCptQstRequestDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;

import kr.ac.dhuniv.core_cpt.repository.CoreCptOptionTemplateRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptQstRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ✅ CoreCptQstService
 * - 진단 문항 및 선택지에 대한 비즈니스 로직을 처리
 */
@Service
@RequiredArgsConstructor
public class CoreCptQstService {

    private final CoreCptQstRepository qstRepository;           // 진단 문항 리포지토리
    private final CoreCptOptionTemplateRepository optionRepository;
    private final CoreCptInfoRepository cptInfoRepository;      // 역량 정보 리포지토리

    /**
     * ✅ 조건 기반 문항 검색
     * - Repository에서 Page<CoreCptQst>를 조회하고 map으로 DTO 변환
     *
     * @param topCptId 상위 역량 ID (nullable)
     * @param subCptId 하위 역량 ID (nullable)
     * @param pageable 페이지 정보 (page, size, sort 등)
     * @param keyword 문항 내용 검색어 (nullable)
     * @return Page<CoreCptQstListDTO> 변환된 DTO 페이지
     */
    public Page<CoreCptQstListDTO> filterQuestionList(Long topCptId, Long subCptId, Pageable pageable, String keyword) {
        // (1) qstRepository에서 조건 기반 페이징 조회 실행
        Page<CoreCptQst> page = qstRepository.filter(topCptId, subCptId, keyword, pageable);

        // (2) 조회된 Page<CoreCptQst>를 Page<CoreCptQstListDTO>로 변환
        return page.map(q -> {
            CoreCptInfo subCpt = q.getCoreCptInfo();           // 하위 역량 엔티티
            CoreCptInfo topCpt = subCpt.getParent();           // 상위 역량 엔티티 (nullable)

            return CoreCptQstListDTO.builder()
                    .qstId(q.getQstId())                       // 문항 ID
                    .qstCode(q.getQstCode())                   // 문항 코드
                    .questionText(q.getQstCont())              // 문항 내용
                    .competencyName(topCpt != null ? topCpt.getCciNm() : "")  // 상위 역량명
                    .subCompetencyName(subCpt.getCciNm())      // 하위 역량명
                    .colorHex(topCpt != null ? topCpt.getColorHex() : "#999") // 상위 역량 색상 (기본값 #999)
                    .build();
        });
    }
    /**
     * ✅ 문항 등록
     *
     * @param dto 등록할 문항 DTO
     */
    public void addQuestion(CoreCptQstRequestDTO dto) {
        // (1) 하위 역량 조회
        CoreCptInfo subCpt = cptInfoRepository.findById(dto.getCoreCptInfoId())
                .orElseThrow(() -> new IllegalArgumentException("역량 없음: ID=" + dto.getCoreCptInfoId()));

        // (2) 선택지 템플릿 조회 (리커트 공통 ID 지정)
        CoreCptOptionTemplate optionTemplate = optionRepository.findById(dto.getOptionTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("선택지 템플릿 없음: ID=" + dto.getOptionTemplateId()));

        // (3) 문항 저장
        CoreCptQst entity = CoreCptQst.builder()
                .qstCode(dto.getQstCode())
                .qstCont(dto.getQuestionText())
                .qstOrd(dto.getQstOrd())
                .coreCptInfo(subCpt)
                .optionTemplate(optionTemplate)
                .regUserId(dto.getRegUserId())
                .regDt(LocalDateTime.now())
                .build();

        qstRepository.save(entity);
    }

    /**
     * ✅ 진단 문항 삭제
     * @param qstId 삭제할 문항 ID
     */
    public void deleteQuestion(Long qstId) {
        qstRepository.deleteById(qstId); // CASCADE 로 옵션도 같이 삭제됨
    }

    /**
     * ✅ 진단 문항 목록 조회 (간단 예시)
     * @return 문항 DTO 리스트
     */
    public List<CoreCptQstListDTO> getQuestionList() {
        List<CoreCptQst> questions = qstRepository.findAll();
        List<CoreCptQstListDTO> result = new ArrayList<>();

        for (CoreCptQst q : questions) {
            CoreCptInfo sub = q.getCoreCptInfo();                // 하위 역량
            CoreCptInfo top = sub.getParent();                   // 상위 역량

            result.add(CoreCptQstListDTO.builder()
                    .qstId(q.getQstId())
                    .qstCode(q.getQstCode())
                    .questionText(q.getQstCont())
                    .competencyName(q.getCoreCptInfo().getCciNm())
                    .topCompetencyName(top != null ? top.getCciNm() : "-")  // 상위 역량명
                    .colorHex(top != null ? top.getColorHex() : "#6c757d") // default gray
                    .subCompetencyName(sub.getCciNm())                      // 하위 역량명
                    .build());
        }

        return result;
    }

    /**
     * ✅ 하위 역량의 다음 문항 코드 생성
     *
     * @param subCptId 하위 역량 ID
     * @return 생성된 문항 코드
     */
    public String generateNextQstCode(Long subCptId) {
        // (1) 하위 역량 존재 여부 확인
        CoreCptInfo subCpt = cptInfoRepository.findById(subCptId)
                .orElseThrow(() -> new IllegalArgumentException("하위 역량을 찾을 수 없습니다: ID=" + subCptId));

        // (2) 기존 최대 문항 코드 조회
        String maxCode = qstRepository.findMaxQstCodeBySubCpt(subCptId);

        // (3) 순번 계산
        int nextSeq = 1;  // 기본 1번
        if (maxCode != null && maxCode.contains("-")) {
            try {
                String[] parts = maxCode.split("-");
                nextSeq = Integer.parseInt(parts[1]) + 1;
            } catch (NumberFormatException e) {
                nextSeq = 1;  // fallback
            }
        }

        // (4) 새 코드 생성 (하위 역량 코드 + - + 2자리 순번)
        return subCpt.getCciCode() + "-" + String.format("%02d", nextSeq);
    }
}

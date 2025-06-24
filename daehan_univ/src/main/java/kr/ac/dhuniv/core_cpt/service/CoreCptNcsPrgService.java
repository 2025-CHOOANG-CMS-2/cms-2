package kr.ac.dhuniv.core_cpt.service;


import kr.ac.dhuniv.core_cpt.dto.ncs.NcsPrgViewDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCpt_MileageRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCpt_NcsPrgInfoRepository;
import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreCptNcsPrgService {
    private final CoreCpt_NcsPrgInfoRepository prgInfoRepository;
    private final CoreCpt_MileageRepository mileageRepository;
    /**
     * 추천 비교과 프로그램 목록을 조회하여 DTO 리스트로 변환
     *
     * @return 추천 비교과 프로그램 DTO 리스트
     */
    public List<NcsPrgViewDTO> getRecommendedPrograms() {
        // 모든 비교과 프로그램 정보 조회
        List<NcsPrgInfo> programs = prgInfoRepository.findAll();

        // 엔티티를 DTO로 매핑
        return programs.stream().map(prg -> {
            // 해당 프로그램의 마일리지 정보 조회
            NcsPrgMileage mileage = mileageRepository.findByProgram(prg);

            // 해당 프로그램의 신청자 수 집계 (커스텀 쿼리 활용)
            int appliedCount = prgInfoRepository.countAppliedStudents(prg.getPrgId());

            // DTO 생성 및 반환
            return NcsPrgViewDTO.builder()
                    .prgId(prg.getPrgId())                         // 프로그램 ID
                    .prgNm(prg.getPrgNm())                         // 프로그램명
                    .prgDesc(prg.getPrgDesc())                     // 프로그램 설명
                    .coreCptName(prg.getCoreCpt().getCciNm())      // 핵심역량명
                    .coreCptColorHex(prg.getCoreCpt().getColorHex())// 핵심역량 색상
                    .mileageScore(mileage != null ? mileage.getMileageScore() : null) // 마일리지 점수
                    .prgCapacity(prg.getMaxCnt())             // 모집 인원
                    .appliedCount(appliedCount)                    // 신청자 수
                    .prgEndDate(prg.getPrgEndDate())               // 마감일자
                    .build();
        }).collect(Collectors.toList());
    }
}

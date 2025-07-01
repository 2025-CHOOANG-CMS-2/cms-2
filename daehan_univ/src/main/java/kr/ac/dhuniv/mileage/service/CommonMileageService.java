package kr.ac.dhuniv.mileage.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.mileage.dto.CoreCptForMlgDTO;
import kr.ac.dhuniv.mileage.dto.NcsPrgForMlgDTO;
import kr.ac.dhuniv.mileage.repository.CoreCptInfoRepository2;
import kr.ac.dhuniv.mileage.repository.NcsPrgInfoRepository2;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonMileageService {
	
    private final CoreCptInfoRepository2 coreCptInfoRepo;
    private final NcsPrgInfoRepository2 ncsPrgInfoRepo;
    
    // 핵심역량 목록
    public List<CoreCptForMlgDTO> getAllCoreCompetencies() {
        List<CoreCptInfo> entities = coreCptInfoRepo.findByParentIsNull();
        return entities.stream()
        		.sorted(Comparator.comparing(CoreCptInfo::getCciNm, Comparator.nullsLast(Comparator.naturalOrder()))) // 사전순 정렬
                .map(c -> new CoreCptForMlgDTO(c.getCciId(), c.getCciNm()))
                .collect(Collectors.toList());
    }
    
    // 비교과 프로그램 목록
    public List<NcsPrgForMlgDTO> getAllNcsPrograms() {
        List<NcsPrgInfo> entities = ncsPrgInfoRepo.findAll();
        return entities.stream()
        		.sorted(Comparator.comparing(NcsPrgInfo::getPrgNm, Comparator.nullsLast(Comparator.naturalOrder()))) // 사전순 정렬
                .map(c -> new NcsPrgForMlgDTO(c.getPrgId(), c.getPrgNm()))
                .collect(Collectors.toList());
    }
}

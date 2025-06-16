package kr.ac.dhuniv.counsel.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.repository.CnslrInfoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorAdminServiceImpl implements CounselorAdminService {

    private final CnslrInfoRepository cnslrInfoRepository;

    @Override
    public List<CounselorListDto> getCounselorList() {
        return cnslrInfoRepository.findCounselorList();
    }
}
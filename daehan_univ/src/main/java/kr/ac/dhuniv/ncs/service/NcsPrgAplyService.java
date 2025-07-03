package kr.ac.dhuniv.ncs.service;

import kr.ac.dhuniv.ncs.dto.MyProgramDto;
import kr.ac.dhuniv.ncs.dto.NcsPrgAplyDto;
import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.mapper.NcsPrgAplyMapper;
import kr.ac.dhuniv.ncs.mapper.NcsPrgInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NcsPrgAplyService {

    private final NcsPrgInfoMapper ncsPrgInfoMapper;
    private final NcsPrgAplyMapper ncsPrgAplyMapper;

    public NcsPrgAplyService(NcsPrgInfoMapper ncsPrgInfoMapper, NcsPrgAplyMapper ncsPrgAplyMapper) {
        this.ncsPrgInfoMapper = ncsPrgInfoMapper;
        this.ncsPrgAplyMapper = ncsPrgAplyMapper;
    }

    /**
     * 학생이 프로그램에 신청하는 메서드
     * @param prgId 신청할 프로그램 ID
     * @param stdId 신청하는 학생 ID
     */
    @Transactional
    public void applyForProgram(Long prgId, Long stdId) {
        // 1. 중복 신청 확인
        Map<String, Object> params = new HashMap<>();
        params.put("prgId", prgId);
        params.put("stdId", stdId);
        int count = ncsPrgAplyMapper.existsByProgramIdAndStudentId(params);
        if (count > 0) {
            throw new IllegalStateException("이미 신청한 프로그램입니다.");
        }

        // 2. 모집 인원 확인
        ProgramDto program = ncsPrgInfoMapper.selectOne(prgId);
        if (program == null) {
            throw new IllegalArgumentException("존재하지 않는 프로그램입니다.");
        }
        int maxCount = program.getMaxCnt();
        int currentCount = ncsPrgAplyMapper.countByProgramId(prgId);
        if (currentCount >= maxCount) {
            throw new IllegalStateException("모집 인원이 마감되었습니다.");
        }

        // 3. 모든 검증 통과 시 신청 정보 저장
        NcsPrgAplyDto aplyDto = new NcsPrgAplyDto();
        aplyDto.setPrgId(prgId);
        aplyDto.setStdId(stdId);
        aplyDto.setAplyStatCd("APPLIED");
        aplyDto.setAplyCode("PRG" + prgId + "-STD" + stdId);

        ncsPrgAplyMapper.insert(aplyDto);
    }
    
    public List<MyProgramDto> getMyApplicationStatus(Long stdId) {
        List<MyProgramDto> myPrograms = ncsPrgAplyMapper.findMyProgramsByStudentId(stdId);
        LocalDateTime now = LocalDateTime.now();

        // DB에서 조회한 데이터를 바탕으로 최종 상태 결정
        for (MyProgramDto dto : myPrograms) {
            // 이수/미수료/취소 정보가 있으면 그것이 최종 상태
            if ("CMP".equals(dto.getCmpStatCd())) {
                dto.setOverallStatus("completed");
                dto.setOverallStatusText("이수");
            } else if ("NCP".equals(dto.getCmpStatCd())) {
                dto.setOverallStatus("rejected"); // '미이수'는 '반려/거절'과 유사한 상태로 간주
                dto.setOverallStatusText("미이수");
            } else if ("CAN".equals(dto.getCmpStatCd())) {
                dto.setOverallStatus("rejected");
                dto.setOverallStatusText("신청취소");
            }
            // 진행중 정보가 있거나, 프로그램 기간이 현재 포함되면 '진행중'
            else if ("ING".equals(dto.getCmpStatCd()) || (now.isAfter(dto.getPrgStDt()) && now.isBefore(dto.getPrgEndDt()))) {
                dto.setOverallStatus("ongoing");
                dto.setOverallStatusText("진행중");
            }
            // 그 외의 경우는 모두 '대기중' (승인 대기)
            else {
                dto.setOverallStatus("pending");
                dto.setOverallStatusText("대기중");
            }
        }
        return myPrograms;
    }
}
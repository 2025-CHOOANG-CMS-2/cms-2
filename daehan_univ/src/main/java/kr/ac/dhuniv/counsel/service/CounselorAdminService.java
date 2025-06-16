package kr.ac.dhuniv.counsel.service;

import java.util.List;

import kr.ac.dhuniv.counsel.dto.CounselorListDto;

public interface CounselorAdminService {
	List<CounselorListDto> getCounselorList();
}

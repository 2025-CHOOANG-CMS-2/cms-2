//package kr.ac.dhuniv.counsel.service;
//
//import java.util.List;
//
//import kr.ac.dhuniv.counsel.dto.CounselorListDto;
//import kr.ac.dhuniv.counsel.dto.CreateCounselorRequestDto;
//import kr.ac.dhuniv.counsel.dto.UnregisteredEmpDto;
//import kr.ac.dhuniv.counsel.dto.UpdateCounselorRequestDto;
//
//public interface CounselorAdminService {
//	List<CounselorListDto> getCounselorList();
//	
//	CounselorListDto getCounselorDetail(String counselorId);
//	
//	void updateCounselor(String counselorId, UpdateCounselorRequestDto requestDto);
//	
//	List<UnregisteredEmpDto> getUnregisteredEmployees();
//	
//	void createCounselor(CreateCounselorRequestDto requestDto);
//	
//	void deleteCounselor(String counselorId);
//	
//	void updateCounselorStatus(String counselorId, boolean isActive);
//}

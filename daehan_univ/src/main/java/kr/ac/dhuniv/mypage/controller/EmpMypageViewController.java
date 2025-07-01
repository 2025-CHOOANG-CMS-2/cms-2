package kr.ac.dhuniv.mypage.controller;

import kr.ac.dhuniv.empl_info.dto.EmplInfoDto;
import kr.ac.dhuniv.mypage.service.EmpMypageService;
import kr.ac.dhuniv.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping; // RequestMapping 임포트 추가
import org.springframework.web.bind.annotation.RequestParam; // RequestParam 임포트 추가

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/employee_mypage.do") // 클래스 레벨 URL 매핑
public class EmpMypageViewController {

    private final EmpMypageService empMypageService;

    @Autowired
    public EmpMypageViewController(EmpMypageService empMypageService) {
        this.empMypageService = empMypageService;
    }

    /**
     * 교직원 마이페이지를 렌더링하고 필요한 데이터를 모델에 추가합니다.
     * URL 파라미터 'employeeId'가 제공되면 해당 교직원 ID의 정보를 조회하고,
     * 그렇지 않으면 현재 로그인된 사용자의 교직원 ID를 사용합니다.
     *
     * @param user 현재 인증된 사용자 정보 (Spring Security Principal, 없을 수도 있음)
     * @param employeeIdParam URL 쿼리 파라미터로 전달된 교직원 ID (선택 사항)
     * @param model Thymeleaf로 전달할 데이터를 담는 Model 객체
     * @return 템플릿 경로 ("mypage/employee-mypage.html")
     */
    @GetMapping // GET /employee_mypage.do 또는 /employee_mypage.do?employeeId={교직원ID}
    public String employeeMypage(
            @AuthenticationPrincipal User user,
            @RequestParam(name = "employeeId", required = false) String employeeIdParam,
            Model model) {

        String targetEmployeeId = null;

        // 1. URL 파라미터에 employeeId가 제공되면 우선적으로 사용
        if (employeeIdParam != null && !employeeIdParam.trim().isEmpty()) {
            targetEmployeeId = employeeIdParam.trim();
            System.out.println("✅ [EmpMypage Controller] URL 파라미터에서 교직원 ID 감지: " + targetEmployeeId);
        } else if (user != null && user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
            // 2. URL 파라미터가 없으면 로그인된 사용자의 교직원 ID 사용
            targetEmployeeId = user.getUserId().trim();
            System.out.println("✅ [EmpMypage Controller] 로그인된 사용자 교직원 ID 사용: " + targetEmployeeId);
        } else {
            // 3. 둘 다 없는 경우, 빈 DTO로 페이지 로드
            System.out.println("❌ [EmpMypage Controller] 교직원 정보를 조회할 ID를 찾을 수 없습니다. 빈 정보로 페이지를 로드합니다.");
            // 리다이렉트 없이 빈 EmplInfoDto를 전달하여 페이지 로드 유지
            model.addAttribute("emplInfo", new EmplInfoDto());
            model.addAttribute("departmentMap", empMypageService.getDepartmentCodeNameMap());
            model.addAttribute("positionMap", empMypageService.getPositionCodeNameMap());
            return "mypage/employee-mypage"; // 템플릿 경로
        }

        // 이제 targetEmployeeId를 사용하여 서비스 호출
        Optional<EmplInfoDto> emplInfoDtoOptional = empMypageService.getEmplInfoDtoByUserId(targetEmployeeId);
        
        if (emplInfoDtoOptional.isPresent()) {
            EmplInfoDto emplInfoDto = emplInfoDtoOptional.get();
            model.addAttribute("emplInfo", emplInfoDto); // DTO 객체를 모델에 추가
            System.out.println("✅ [EmpMypage Controller] 교직원 정보 발견: " + emplInfoDto.getSTAFF_NM() + " (교직원 ID: " + emplInfoDto.getSTAFF_NO() + ")");
            
            // DTO에서 프로필 이미지 URL 가져와 모델에 추가
            if (emplInfoDto.getPROFILE_IMAGE_URL() != null && !emplInfoDto.getPROFILE_IMAGE_URL().isEmpty()) {
                model.addAttribute("profileImageUrl", emplInfoDto.getPROFILE_IMAGE_URL());
            } else {
                model.addAttribute("profileImageUrl", "/images/default_profile.png"); // 기본 이미지 경로
            }

        } else {
            // 교직원 정보가 없는 경우 (예: User는 있지만 연결된 EmplInfo가 없는 경우)
            model.addAttribute("emplInfo", new EmplInfoDto()); // 템플릿에서 null 체크 필요 없도록 빈 DTO 전달
            model.addAttribute("errorMessage", "조회 요청한 교직원 정보를 찾을 수 없습니다. 교직원 ID를 확인해주세요.");
            System.out.println("❌ [EmpMypage Controller] 교직원 정보를 찾을 수 없음: 조회 대상 교직원 ID " + targetEmployeeId);
            model.addAttribute("profileImageUrl", "/images/default_profile.png"); // 교직원 정보 없을 때도 기본 이미지
        }
        
        // 학과 및 직급 맵을 모델에 추가 (서비스에서 가져오는 부분 유지)
        model.addAttribute("departmentMap", empMypageService.getDepartmentCodeNameMap());
        model.addAttribute("positionMap", empMypageService.getPositionCodeNameMap());

        return "mypage/employee-mypage"; // Thymeleaf 템플릿 경로
    }
}

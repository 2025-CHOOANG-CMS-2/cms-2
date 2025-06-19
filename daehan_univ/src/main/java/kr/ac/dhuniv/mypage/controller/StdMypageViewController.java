package kr.ac.dhuniv.mypage.controller;

// import kr.ac.dhuniv.std_info.domain.StdInfo; // 이제 엔티티를 직접 사용하지 않으므로 제거하거나 주석 처리
import kr.ac.dhuniv.std_info.dto.StdInfoDto; // DTO 임포트
import kr.ac.dhuniv.mypage.service.StdMypageService;
import kr.ac.dhuniv.user.User; // Spring Security를 통해 주입될 User 엔티티
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 로그인된 사용자 정보 주입
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import java.util.Map;

@Controller
@RequestMapping("/student_mypage.do") // URL 매핑을 클래스 레벨로 변경
public class StdMypageViewController {

    private final StdMypageService stdMypageService;

    @Autowired
    public StdMypageViewController(StdMypageService stdMypageService) {
        this.stdMypageService = stdMypageService;
    }

    /**
     * 학생 마이페이지 조회 화면을 렌더링합니다.
     * @param user 현재 로그인된 사용자 객체 (Spring Security에 의해 자동 주입)
     * @param model Thymeleaf 템플릿으로 데이터를 전달하기 위한 Model 객체
     * @return 마이페이지 템플릿 이름 ("/mypage/student-mypage.html")
     */
    @GetMapping // GET /student_mypage.do
    public String showStudentMypage(@AuthenticationPrincipal User user, Model model) {
        // 1. 사용자 로그인 상태 확인
        if (user == null) {
            System.out.println("DEBUG: 사용자가 로그인되어 있지 않습니다. 로그인 페이지로 리다이렉트.");
            return "redirect:/login"; // 실제 로그인 페이지 경로로 수정 필요
        }

        System.out.println("DEBUG: 로그인된 사용자 ID: " + user.getUserId());

        // 2. StdMypageService의 getMypageInfo(User user)를 호출하여 StdInfoDto 조회
        Optional<StdInfoDto> stdInfoDtoOptional = stdMypageService.getMypageInfo(user);
        
        if (stdInfoDtoOptional.isPresent()) {
            StdInfoDto stdInfoDto = stdInfoDtoOptional.get();
            model.addAttribute("stdInfo", stdInfoDto); // DTO 객체를 모델에 추가
            System.out.println("학생 정보 발견: " + stdInfoDto.getSTD_NM());
            
            // DTO에서 프로필 이미지 URL 가져와 모델에 추가
            if (stdInfoDto.getPROFILE_IMAGE_URL() != null && !stdInfoDto.getPROFILE_IMAGE_URL().isEmpty()) {
                model.addAttribute("profileImageUrl", stdInfoDto.getPROFILE_IMAGE_URL());
            } else {
                model.addAttribute("profileImageUrl", "/images/default_profile.png"); // 기본 이미지 경로
            }

        } else {
            // 학생 정보가 없는 경우 (예: User는 있지만 연결된 StdInfo가 없는 경우)
            model.addAttribute("stdInfo", null); // 템플릿에서 null 체크 필요
            model.addAttribute("errorMessage", "로그인된 사용자의 학생 정보를 찾을 수 없습니다. 정보를 등록해주세요.");
            System.out.println("학생 정보를 찾을 수 없음: 로그인된 사용자 ID " + user.getUserId());
            model.addAttribute("profileImageUrl", "/images/default_profile.png"); // 학생 정보 없을 때도 기본 이미지
        }
        
        // 학과 코드 맵을 모델에 추가
        Map<String, String> departmentMap = stdMypageService.getDepartmentCodeNameMap();
        model.addAttribute("departmentMap", departmentMap);

        return "/mypage/student-mypage.html"; // Thymeleaf 템플릿 경로
    }
}

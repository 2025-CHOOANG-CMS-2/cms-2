package kr.ac.dhuniv.mypage.controller;

import kr.ac.dhuniv.std_info.dto.StdInfoDto; // DTO 임포트
import kr.ac.dhuniv.mypage.service.StdMypageService;
import kr.ac.dhuniv.user.User; // Spring Security를 통해 주입될 User 엔티티
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 로그인된 사용자 정보 주입
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 임포트
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/students/mypage") // URL 매핑을 클래스 레벨로 변경
public class StdMypageViewController {

    private final StdMypageService stdMypageService;

    @Autowired
    public StdMypageViewController(StdMypageService stdMypageService) {
        this.stdMypageService = stdMypageService;
    }

    /**
     * 학생 마이페이지 조회 화면을 렌더링합니다.
     * URL 파라미터 'studentId'가 제공되면 해당 학번의 정보를 조회하고,
     * 그렇지 않으면 현재 로그인된 사용자의 학번을 사용합니다.
     *
     *
     * @param user 현재 로그인된 사용자 객체 (Spring Security에 의해 자동 주입될 수 있음)
     * @param studentIdParam URL 쿼리 파라미터로 전달된 학생 학번 (선택 사항)
     * @param model Thymeleaf 템플릿으로 데이터를 전달하기 위한 Model 객체
     * @param redirectAttributes 리다이렉트 시 플래시 메시지를 전달하기 위한 객체
     * @return 마이페이지 템플릿 이름 ("/mypage/student-mypage.html") 또는 로그인 페이지로 리다이렉트
     */
    @GetMapping //        ex : /students/mypage?studentId=2025004001
    public String showStudentMypage(
            @AuthenticationPrincipal User user, // 로그인된 사용자 (있을 수도 있고 없을 수도 있음)
            @RequestParam(name = "studentId", required = false) String studentIdParam, // URL 파라미터로 학번 받기
            Model model,
            RedirectAttributes redirectAttributes) {

        String targetStudentId = null;

        // 1. URL 파라미터에 studentId가 제공되면 우선적으로 사용
        if (studentIdParam != null && !studentIdParam.trim().isEmpty()) {
            targetStudentId = studentIdParam.trim();
            System.out.println("✅ [Mypage Controller] URL 파라미터에서 학번 감지: " + targetStudentId);
        } else if (user != null && user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
            // 2. URL 파라미터가 없으면 로그인된 사용자의 학번 사용
            targetStudentId = user.getUserId().trim();
            System.out.println("✅ [Mypage Controller] 로그인된 사용자 학번 사용: " + targetStudentId);
        } else {
            // 3. 둘 다 없는 경우, 로그인 페이지로 리다이렉트
            System.out.println("❌ [Mypage Controller] 학생 정보를 조회할 학번을 찾을 수 없습니다. 로그인 페이지로 리다이렉트합니다.");
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 정보가 유효하지 않거나, 조회할 학생 학번이 제공되지 않았습니다.");
            return "mypage/student-mypage"; // 실제 로그인 페이지 URL로 리다이렉트 (프로젝트에 맞게 수정 필요)
        }

        // 이제 targetStudentId를 사용하여 서비스 호출
        Optional<StdInfoDto> stdInfoDtoOptional = stdMypageService.getStdInfoDtoByUserId(targetStudentId);
        
        if (stdInfoDtoOptional.isPresent()) {
            StdInfoDto stdInfoDto = stdInfoDtoOptional.get();
            model.addAttribute("stdInfo", stdInfoDto); // DTO 객체를 모델에 추가
            System.out.println("✅ [Mypage Controller] 학생 정보 발견: " + stdInfoDto.getSTD_NM() + " (학번: " + stdInfoDto.getSTD_NO() + ")");
            
            // DTO에서 프로필 이미지 URL 가져와 모델에 추가
            if (stdInfoDto.getPROFILE_IMAGE_URL() != null && !stdInfoDto.getPROFILE_IMAGE_URL().isEmpty()) {
                model.addAttribute("profileImageUrl", stdInfoDto.getPROFILE_IMAGE_URL());
            } else {
                model.addAttribute("profileImageUrl", "/images/default_profile.png"); // 기본 이미지 경로
            }

        } else {
            // 학생 정보가 없는 경우 (예: User는 있지만 연결된 StdInfo가 없는 경우)
            model.addAttribute("stdInfo", null); // 템플릿에서 null 체크 필요
            model.addAttribute("errorMessage", "조회 요청한 학생 정보를 찾을 수 없습니다. 학번을 확인해주세요.");
            System.out.println("❌ [Mypage Controller] 학생 정보를 찾을 수 없음: 조회 대상 학번 " + targetStudentId);
            model.addAttribute("profileImageUrl", "/images/default_profile.png"); // 학생 정보 없을 때도 기본 이미지
        }
        
        // 학과 코드 맵을 모델에 추가 (서비스에서 가져오는 부분은 유지)
        Map<String, String> departmentMap = stdMypageService.getDepartmentCodeNameMap();
        model.addAttribute("departmentMap", departmentMap);

        return "mypage/student-mypage"; // Thymeleaf 템플릿 경로 (확장자 .html 제거 권장)
    }
}

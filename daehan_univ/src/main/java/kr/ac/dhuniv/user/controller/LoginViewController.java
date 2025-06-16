package kr.ac.dhuniv.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginViewController {
    @GetMapping({"/login","/"})
    public String getLoginView() {
        return "login";
    }

    @GetMapping("/admin/index")
    public String getAdminIndexController(Authentication authentication) {
        //System.out.println("authentication.getName() = " + authentication.getName());
        // ✅ 현재 로그인 사용자의 권한 목록 가져오기
        if (authentication == null || authentication.getAuthorities() == null) {
            System.out.println("❌ 인증 정보 없음, 접근 거부");
            return "error/403";  // 403 에러 페이지 or 리다이렉트
        }
        
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!hasAdminRole) {
            System.out.println("❌ ADMIN 권한 없음, 접근 거부");
            return "error/403";  // 403 에러 페이지 or 리다이렉트
        }

        // ✅ ADMIN 권한 있으면 정상 페이지 반환
        System.out.println("✅ ADMIN 권한 확인됨, 페이지 반환");
        return "/admin/admin-index";
    }

    @GetMapping("/student/index")
    public String getStudentIndexController(Authentication authentication) {
        //System.out.println("authentication.getName() = " + authentication.getName());
        // ✅ 현재 로그인 사용자의 권한 목록 가져오기
        if (authentication == null || authentication.getAuthorities() == null) {
            System.out.println("❌ 인증 정보 없음, 접근 거부");
            return "error/403";  // 403 에러 페이지 or 리다이렉트
        }

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"));

        if (!hasAdminRole) {
            System.out.println("❌ STUDENT 권한 없음, 접근 거부");
            return "error/403";  // 403 에러 페이지 or 리다이렉트
        }

        // ✅ ADMIN 권한 있으면 정상 페이지 반환
        System.out.println("✅ STUDENT 권한 확인됨, 페이지 반환");
        return "/student/index";
    }

    @GetMapping("/employees/index")
    public String getEmployeesIndexController(Authentication authentication) {
        //System.out.println("authentication.getName() = " + authentication.getName());
        // ✅ 현재 로그인 사용자의 권한 목록 가져오기
        if (authentication == null || authentication.getAuthorities() == null) {
            System.out.println("❌ 인증 정보 없음, 접근 거부");
            return "error/403";  // 403 에러 페이지 or 리다이렉트
        }

        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));

        if (!hasAdminRole) {
            System.out.println("❌ EMPLOYEE 권한 없음, 접근 거부");
            return "error/403";  // 403 에러 페이지 or 리다이렉트
        }

        // ✅ ADMIN 권한 있으면 정상 페이지 반환
        System.out.println("✅ EMPLOYEE 권한 확인됨, 페이지 반환");
        return "/employee/index";
    }

}

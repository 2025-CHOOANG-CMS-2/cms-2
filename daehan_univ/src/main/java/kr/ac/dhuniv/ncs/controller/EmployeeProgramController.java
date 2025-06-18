package kr.ac.dhuniv.ncs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 교직원 비교과 프로그램 관리 뷰 컨트롤러
 */
@Controller
public class EmployeeProgramController {

    @GetMapping("/employee/program")
    public String viewAdminProgramListPage() {
        return "employee/program/index"; // templates/admin/program/list.html
    }
}

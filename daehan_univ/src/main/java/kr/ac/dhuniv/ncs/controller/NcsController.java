package kr.ac.dhuniv.ncs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NcsController {
	 @GetMapping("/employee/programs")
	    public String programsPage() {
	        return "employee/program/index";
	    }
}

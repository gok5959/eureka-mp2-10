package com.mycom.myapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainViewController {

    @GetMapping("/testmain")
    public String main(Model model) {
        model.addAttribute("currentUserId", 1L);
        model.addAttribute("currentUserName", "사용자 1");
        return "templates/main.html";
    }
}

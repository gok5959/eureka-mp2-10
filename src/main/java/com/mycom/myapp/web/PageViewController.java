package com.mycom.myapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class PageViewController {

    @GetMapping("/testmain")
    public String main(Model model) {
        model.addAttribute("currentUserId", 1L);
        model.addAttribute("currentUserName", "사용자 1");
        return "main"; // (네 프로젝트 설정이 이렇게 먹는다면 유지)
    }

    @GetMapping("/groups/{groupId}/calendar")
    public String groupCalendar(@PathVariable("groupId") Long groupId, Model model) {
        model.addAttribute("currentUserId", 1L);
        model.addAttribute("currentUserName", "사용자 1");
        model.addAttribute("groupId", groupId);
        return "group-calendar";
    }
}

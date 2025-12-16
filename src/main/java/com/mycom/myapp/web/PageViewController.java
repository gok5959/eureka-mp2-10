package com.mycom.myapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class PageViewController {

    @GetMapping("/main")
    public String main() {
        return "main";
    }

    @GetMapping("/groups/{groupId}/calendar")
    public String groupCalendar(@PathVariable("groupId") Long groupId, Model model) {
        model.addAttribute("groupId", groupId);
        return "group-calendar";
    }
}

package com.mycom.myapp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class WebPageController {

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/pages/login.html";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "forward:/pages/signup.html";
    }

    @GetMapping("/groups/page/new")
    public String createGroupPage() {
        return "forward:/pages/group-create.html";
    }

    @GetMapping("/groups/page/{groupId}/edit")
    public String editGroupPage(@PathVariable Long groupId) {
        return "forward:/pages/group-edit.html";
    }

    @GetMapping("/groups/page/{groupId}")
    public String groupDetailPage(@PathVariable Long groupId) {
        return "forward:/pages/group-detail.html";
    }
}

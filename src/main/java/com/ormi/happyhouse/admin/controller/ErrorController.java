package com.ormi.happyhouse.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {
    @GetMapping("/401")
    public String unauthorized() {
        return "error/401";
    }
    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}

package com.rks.collections.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")         public String index()    { return "index"; }
    @GetMapping("/index")    public String index2()   { return "index"; }
    @GetMapping("/login-page")    public String login()    { return "login"; }
    @GetMapping("/register-page") public String register() { return "register"; }
    @GetMapping("/admin")    public String admin()    { return "admin"; }
}

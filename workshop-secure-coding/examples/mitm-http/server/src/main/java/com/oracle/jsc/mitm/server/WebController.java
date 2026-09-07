package com.oracle.jsc.mitm.server;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/form")
    public String form(Model model) {
        model.addAttribute("loginDto", new LoginDto());

        return "login_form";
    }

    private void logHeaders(HttpServletRequest request) {
        logger.info("---- logging headers for login page request");
        for (Enumeration<String> headerNames = request.getHeaderNames();
                headerNames.hasMoreElements();) {
            String name = headerNames.nextElement();
            logger.info("{}={}", name, request.getHeader(name));
        }
        logger.info("----");
    }

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        logHeaders(request);

        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginDto login, Model model, HttpServletRequest request) {
        // perform registration action here...
        model.addAttribute("name", login.getFname() + " " + login.getLname());

        logHeaders(request);

        logger.info("logged_in {}", login.getFname() + " " + login.getLname());
        return "logged_in";
    }
}

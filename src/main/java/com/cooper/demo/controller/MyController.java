package com.cooper.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyController {
    @RequestMapping("/doupload")
    public String upload()
    {
        return "/File/upload_file";
    }
}

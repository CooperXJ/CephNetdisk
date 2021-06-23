package com.cooper.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class ChatController {
    @GetMapping("/chat")
    public String toChatPage()
    {
        return "Chat/chat_back";
    }

}

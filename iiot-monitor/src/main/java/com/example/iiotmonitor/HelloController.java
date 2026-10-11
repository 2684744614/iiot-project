package com.example.iiotmonitor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "恭喜！IIoT Monitor 后端服务启动成功！";
    }
}
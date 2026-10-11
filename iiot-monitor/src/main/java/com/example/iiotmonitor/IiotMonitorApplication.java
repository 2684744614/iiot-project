package com.example.iiotmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync   // ★ 开启异步任务支持
public class IiotMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(IiotMonitorApplication.class, args);
	}

}

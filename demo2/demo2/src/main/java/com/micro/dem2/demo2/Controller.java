package com.micro.dem2.demo2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/svc") 
public class Controller {

	@GetMapping("/hello")
	public String hellotest() {
		return "Hello from Service 2";
	}
}

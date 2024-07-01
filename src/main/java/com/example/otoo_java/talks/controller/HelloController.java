package com.example.otoo_java.talks.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

// FastAPI 불러오는 예시
@RestController
@RequestMapping("/api") //이게 루트 주소
public class HelloController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/hello")
    public String sayHello() {
        String fastApiUrl = "http://python-fastapi:8000/fastapi-endpoint";
        String response;
        try {
            response = restTemplate.getForObject(fastApiUrl, String.class);
        } catch (HttpClientErrorException e) {
            response = "Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (ResourceAccessException e) {
            response = "Error: " + e.getMessage();
        }
        return "Hello, World! FastAPI Response: " + response;
    }
}
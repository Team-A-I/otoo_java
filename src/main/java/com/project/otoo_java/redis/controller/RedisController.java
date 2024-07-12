package com.project.otoo_java.redis.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/set")
    public String set(@RequestParam String key, @RequestParam String value) {
        redisTemplate.opsForValue().set(key, value);
        return "Key-Value pair set in Redis";
    }

    @GetMapping("/get")
    public String get(@RequestParam String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }
}
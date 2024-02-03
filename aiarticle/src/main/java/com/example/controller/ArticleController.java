package com.example.controller;

import com.example.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/generateText")
    public String generateText(@RequestParam String url) throws IOException, InterruptedException {
        // 调用TextGenerationService的方法生成文本
        return articleService.generateText(url);
    }
}
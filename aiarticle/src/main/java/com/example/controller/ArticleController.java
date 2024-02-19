package com.example.controller;

import com.example.model.Request;
import com.example.model.Response;
import com.example.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @PostMapping("/process")
    public Response process(@RequestBody Request request) throws IOException, InterruptedException {
        System.out.println("开始处理请求"+request.getUrl());

        // 调用TextGenerationService的方法生成文本和图片
        Response response = articleService.process(request);

        System.out.println("请求处理完成"+request.getUrl());
        return response;
    }
}
package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class ArticleService {
    @Autowired
    private GptService gptService;

    public String generateText(String url) throws IOException, InterruptedException {
        // 定义脚本和图片的路径
        String websiteScreenshotPath = "../screenshot/websiteScreenshot.js";
        String wxocrPath = "../ocr/wxocr.py";
        String screenshotPath = "../screenshot/screenshot.png";

        // 调用websiteScreenshot.js生成screenshot.png
        ProcessBuilder pb = new ProcessBuilder("node", websiteScreenshotPath, url);
        Process process = pb.start();
        process.waitFor();

        // 调用wxocr.py将screenshot.png转换为OCR文本
        pb = new ProcessBuilder("python", wxocrPath, screenshotPath);
        process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        String ocrText = sb.toString();
        process.waitFor();
        // 删除用完的图片
        File file = new File(screenshotPath);
        if(file.exists()){
            file.delete();
        }

        // 调用GptService将OCR文本转换为GPT文本
        return gptService.generateText(ocrText);
    }
}
package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ArticleService {
    @Autowired
    private GptService gptService;

    public String generateText(String url) throws IOException, InterruptedException {
        // 定义脚本和图片的路径
        String screenshotScriptPath = "../screenshot/websiteScreenshot.js";
        String screenshotPngPath = "../screenshot/websiteScreenshot.png";
        String pythonPath = "../ocr/venv/Scripts/python.exe";
        String wxocrScriptPath = "../ocr/wxocr.py";
        String wxocrTxtPath = "../ocr/wxocr.txt";

        // 调用websiteScreenshot.websiteScreenshot.png
        ProcessBuilder pb = new ProcessBuilder("node", screenshotScriptPath, url);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); // 重定向输出到控制台
        Process process = pb.start();
        process.waitFor();

        // 调用wxocr.py将screenshot.png转换为OCR文本
        pb = new ProcessBuilder(pythonPath, wxocrScriptPath, screenshotPngPath);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); // 重定向Python脚本的输出到控制台
        pb.redirectErrorStream(true); // 合并标准错误流和标准输出流
        process = pb.start();
        process.waitFor();

        // 读取OCR识别的文本
        BufferedReader reader = new BufferedReader(new FileReader(wxocrTxtPath));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        String ocrText = sb.toString();
        reader.close();

        // 调用GptService将OCR文本转换为GPT文本
        return gptService.generateText(ocrText);
    }
}
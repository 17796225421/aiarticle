package com.example.service;

import com.example.model.Request;
import com.example.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ArticleService {
    @Autowired
    private GptService gptService;

    public Response process(Request request) throws IOException, InterruptedException {
        // 定义脚本和图片的路径
        String screenshotScriptPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\screenshot\\websiteScreenshot.js";
        String screenshotPngPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\screenshot\\websiteScreenshot.png";
        String pythonPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\ocr\\venv\\Scripts\\python.exe";
        String wxocrScriptPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\ocr\\wxocr.py";
        String wxocrTxtPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\ocr\\wxocr.txt";

        // 调用websiteScreenshot.websiteScreenshot.png
        ProcessBuilder pb = new ProcessBuilder("node", screenshotScriptPath, request.getUrl());
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
        String gptText = gptService.generateText(ocrText);  // 传入gptModel

        // 创建并返回响应
        Response response = new Response();
        response.setGptDesc(gptText);

        // 当 returnImage 为 true 时，才将图片转化为 byte[]
        if (request.isReturnImage()) {
            // 读取图片文件，将其转换为byte[]
            Path imagePath = Paths.get(screenshotPngPath);
            byte[] image = Files.readAllBytes(imagePath);
            response.setImage(image);
        }

        return response;
    }
}
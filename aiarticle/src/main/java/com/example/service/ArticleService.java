package com.example.service;

import com.example.model.Request;
import com.example.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class ArticleService {
    @Autowired
    private GptService gptService;

    public Response process(Request request) throws IOException, InterruptedException {
        // 定义脚本路径
        String screenshotScriptPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\screenshot\\websiteScreenshot.js";
        String pythonPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\ocr\\venv\\Scripts\\python.exe";
        String wxocrScriptPath = "C:\\Users\\zhouzihong\\Desktop\\aiarticle\\ocr\\wxocr.py";

        // 创建临时文件
        File screenshotPngFile = File.createTempFile("websiteScreenshot", ".png");    // 创建临时的websiteScreenshot.png文件
        File wxocrTxtFile = File.createTempFile("wxocr", ".txt");    // 创建临时的wxocr.txt文件
        String screenshotPngPath = screenshotPngFile.getAbsolutePath();    // 获取临时文件的完整路径
        String wxocrTxtPath = wxocrTxtFile.getAbsolutePath();    // 获取临时文件的完整路径

        // 调用websiteScreenshot.websiteScreenshot.png
        ProcessBuilder pb = new ProcessBuilder("node", screenshotScriptPath, request.getUrl(), screenshotPngPath);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT); // 重定向输出到控制台
        Process process = pb.start();
        process.waitFor();

        // 调用wxocr.py将screenshot.png转换为OCR文本
        pb = new ProcessBuilder(pythonPath, wxocrScriptPath, screenshotPngPath, wxocrTxtPath);
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
            // 读取图片文件，将其转换为 BufferedImage
            Path imagePath = Paths.get(screenshotPngPath);
            BufferedImage bufferedImage = ImageIO.read(imagePath.toFile());

            // 创建一个新的 BufferedImage，类型为 TYPE_INT_RGB，不支持透明
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);

            // 把原来的图片绘制到这个新的 BufferedImage 上，TYPE_INT_RGB 类型的 BufferedImage 不支持透明，所以透明部分会被白色填充
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);

            // 将 BufferedImage 压缩为 JPG
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(newBufferedImage, "jpg", byteArrayOutputStream);

            // 将输出流转换为 byte[]
            byte[] image = byteArrayOutputStream.toByteArray();

            // 将 byte[] 转换为 Base64 编码的字符串
            String imageBase64 = Base64.getEncoder().encodeToString(image);

            // 将 base64 编码的字符串存入 response
            response.setImageBase64(imageBase64);
        }

        screenshotPngFile.delete();
        wxocrTxtFile.delete();

        return response;
    }
}
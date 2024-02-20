package com.example.service;

import com.example.model.Request;
import com.example.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
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

            // 将图片进行压缩
            byte[] compressedImage = compressImage(bufferedImage, 500, 1f);

            // 将 byte[] 转换为 Base64 编码的字符串
            String imageBase64 = Base64.getEncoder().encodeToString(compressedImage);

            // 将 base64 编码的字符串存入 response
            response.setImageBase64(imageBase64);
        }

        screenshotPngFile.delete();
        wxocrTxtFile.delete();

        return response;
    }

    /**
     * 按比例压缩图片并转换为灰度图像
     * @param image 原图片
     * @param targetWidth 目标宽度
     * @param quality 目标质量（0.1-1.0）
     * @return byte[] 压缩后的图片
     * @throws IOException
     */
    public byte[] compressImage(BufferedImage image, int targetWidth, float quality) throws IOException {
        // 计算目标高度，保持图片的原始宽高比
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        // 如果长度大于宽度，裁剪到和宽度一样
        if (originalHeight > originalWidth) {
            image = image.getSubimage(0, 0, originalWidth, originalWidth);
            originalHeight = originalWidth;
        }

        int targetHeight = (int) (originalHeight * ((double) targetWidth / originalWidth));

        // 创建一个新的图片对象并转换为灰度图像
        Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);

        // 绘制缩放后的图像
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // 将图片转换为字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(quality);

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        writer.setOutput(ImageIO.createImageOutputStream(out));
        writer.write(null, new IIOImage(newImage, null, null), jpegParams);

        return out.toByteArray();
    }
}
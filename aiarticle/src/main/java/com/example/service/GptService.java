package com.example.service;

import okhttp3.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class GptService {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 连接超时时间
            .writeTimeout(60, TimeUnit.SECONDS) // 写入超时时间
            .readTimeout(120, TimeUnit.SECONDS) // 读取超时时间
            .build();

    private final String apiUrl = "https://api.onechat.fun/v1/chat/completions";
    private final String apiKey = "sk-c25d32ec3a3f64cec6d36b8da55449dac97301dfbd46fc0c";

    // 将OCR文本转换为GPT文本
    public String generateText(String ocrText) throws IOException {
        // 构建JSON请求体
        JsonObject jsonObject = buildJsonRequestBody(ocrText);
        RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

        // 构建请求
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应体
            String responseBody = response.body().string();
            JSONObject responseJson = new JSONObject(responseBody);

            // 获取GPT文本
            String gptContent = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            return gptContent;
        }
    }

    private JsonObject buildJsonRequestBody(String ocrText) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", "gpt-4-all");
        jsonObject.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "system");
        message.addProperty("content", "You are a helpful assistant.");
        messages.add(message);

        message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", ocrText);
        messages.add(message);

        jsonObject.add("messages", messages);
        return jsonObject;
    }
}
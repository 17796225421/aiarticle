package com.example.test;

import com.example.model.Request;
import com.example.model.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentTest {
    private static final String URL = "http://localhost:11000/process";
    // 创建一个固定大小的线程池
    private static ExecutorService service = Executors.newFixedThreadPool(100);
    // 创建一个原子性的计数器
    private static AtomicInteger count = new AtomicInteger(0);
    private static AtomicInteger successCount = new AtomicInteger(0);
    private static AtomicInteger failCount = new AtomicInteger(0);


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();  // 记录开始时间

        // 创建一个请求
        Request request = new Request();
        request.setUrl("https://nihao.co.jp/store");
        request.setReturnImage(true);

        // 创建一个RestTemplate对象，用于发送http请求
        RestTemplate restTemplate = new RestTemplate();

        for (int i = 0; i < 50; i++) {
            service.execute(() -> {
                // 发送http请求，并返回结果
                ResponseEntity<Response> responseEntity = restTemplate.postForEntity(URL, request, Response.class);
                Response response = responseEntity.getBody();
                int statusCode = responseEntity.getStatusCodeValue();
                // 计数器加一
                int currentCount = count.incrementAndGet();

                // 判断是否请求成功
                if (statusCode == 200 && response != null && !"".equals(response.getImageBase64())) {
                    successCount.incrementAndGet();  // 成功次数加一
                    System.out.println("这是返回的第" + currentCount + "次请求，状态码是" + statusCode + "，返回的imageBase64不是空字符串，请求成功。");
                } else {
                    failCount.incrementAndGet();  // 失败次数加一
                    System.out.println("这是返回的第" + currentCount + "次请求，状态码是" + statusCode + "，返回的imageBase64是空字符串，请求失败。");
                }
            });
        }

        // 关闭线程池
        service.shutdown();

        while (!service.isTerminated()) {
            // 等待所有任务结束
        }

        long endTime = System.currentTimeMillis();  // 记录结束时间
        long totalTime = endTime - startTime;  // 计算总时间

        // 打印结果
        System.out.println("总请求次数：" + count.get());
        System.out.println("成功次数：" + successCount.get());
        System.out.println("失败次数：" + failCount.get());
        System.out.println("成功率：" + ((double)successCount.get() / count.get() * 100) + "%");
        System.out.println("失败率：" + ((double)failCount.get() / count.get() * 100) + "%");
        System.out.println("总运行时间：" + totalTime + "ms");
    }
}
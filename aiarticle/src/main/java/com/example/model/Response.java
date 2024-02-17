package com.example.model;

public class Response {
    private String gptDesc;

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    private String imageBase64;

    public String getGptDesc() {
        return gptDesc;
    }

    public void setGptDesc(String gptDesc) {
        this.gptDesc = gptDesc;
    }

    // getters and setters
}
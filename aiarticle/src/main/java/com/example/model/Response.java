package com.example.model;

public class Response {
    private String gptDesc;
    private byte[] image;

    public String getGptDesc() {
        return gptDesc;
    }

    public void setGptDesc(String gptDesc) {
        this.gptDesc = gptDesc;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    // getters and setters
}
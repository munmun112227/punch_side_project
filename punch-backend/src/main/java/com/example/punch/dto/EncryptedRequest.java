package com.example.punch.dto;
import jakarta.validation.constraints.NotBlank;

public class EncryptedRequest {
    @NotBlank
    private String data;
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}

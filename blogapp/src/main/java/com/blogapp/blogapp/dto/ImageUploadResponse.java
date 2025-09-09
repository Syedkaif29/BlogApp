package com.blogapp.blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    
    private Long id;
    private String fileName;
    private String originalName;
    private String url;
    private String contentType;
    private Long fileSize;
}
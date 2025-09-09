package com.blogapp.blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    
    private Long id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private Long usageCount;
}
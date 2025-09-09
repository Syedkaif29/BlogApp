package com.blogapp.blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogSummaryResponse {
    
    private Long id;
    private String title;
    private String contentPreview; // First 200 characters of content
    private AuthorInfo author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}
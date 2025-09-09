package com.blogapp.blogapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String profilePicture;
    private LocalDateTime createdAt;
    private Long blogCount;
    private Long commentCount;
}
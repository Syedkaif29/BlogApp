package com.blogapp.blogapp.controller;

import com.blogapp.blogapp.dto.CommentRequest;
import com.blogapp.blogapp.dto.CommentResponse;
import com.blogapp.blogapp.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @GetMapping("/blogs/{blogId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByBlogId(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getCommentsByBlogId(blogId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    @PostMapping("/blogs/{blogId}/comments")
    public ResponseEntity<?> createComment(
            @PathVariable Long blogId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            CommentResponse response = commentService.createComment(blogId, request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            CommentResponse response = commentService.updateComment(commentId, request, userEmail);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            commentService.deleteComment(commentId, userEmail);
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentResponse>> getUserComments(@PathVariable Long userId) {
        List<CommentResponse> comments = commentService.getCommentsByUserId(userId);
        return ResponseEntity.ok(comments);
    }
}
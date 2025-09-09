package com.blogapp.blogapp.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blogapp.blogapp.dto.BlogRequest;
import com.blogapp.blogapp.dto.BlogResponse;
import com.blogapp.blogapp.dto.BlogSummaryResponse;
import com.blogapp.blogapp.entity.Blog;
import com.blogapp.blogapp.service.BlogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BlogController {
    
    private final BlogService blogService;
    
    /**
     * Get all blogs with pagination, sorting, and searching (public endpoint)
     * GET /api/blogs?page=0&size=10&sortBy=date&search=spring
     */
    @GetMapping
    public ResponseEntity<Page<BlogSummaryResponse>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogs;
        
        // Apply searching
        if (search != null && !search.trim().isEmpty()) {
            blogs = blogService.searchBlogs(search, pageable);
        } else {
            blogs = blogService.getAllBlogs(pageable, sortBy);
        }
        
        Page<BlogSummaryResponse> response = blogs.map(this::convertToBlogSummaryResponse);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get a specific blog by ID (public endpoint)
     * GET /api/blogs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getBlogById(@PathVariable Long id) {
        Optional<Blog> blog = blogService.getBlogByIdAndIncrementViews(id);
        
        if (blog.isPresent()) {
            BlogResponse response = convertToBlogResponse(blog.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create a new blog post (authenticated users only)
     * POST /api/blogs
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BlogResponse> createBlog(
            @Valid @RequestBody BlogRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Blog createdBlog = blogService.createBlog(
            request.getTitle(), 
            request.getContent(), 
            userEmail
        );
        
        BlogResponse response = convertToBlogResponse(createdBlog);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update an existing blog post (author only)
     * PUT /api/blogs/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BlogResponse> updateBlog(
            @PathVariable Long id,
            @Valid @RequestBody BlogRequest request,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            Blog updatedBlog = blogService.updateBlog(
                id, 
                request.getTitle(), 
                request.getContent(), 
                userEmail
            );
            
            BlogResponse response = convertToBlogResponse(updatedBlog);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("only update your own")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    /**
     * Delete a blog post (author only)
     * DELETE /api/blogs/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteBlog(
            @PathVariable Long id,
            Authentication authentication) {
        
        try {
            String userEmail = authentication.getName();
            blogService.deleteBlog(id, userEmail);
            
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("only delete your own")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    /**
     * Get blogs by current authenticated user
     * GET /api/blogs/my-blogs?page=0&size=10
     */
    @GetMapping("/my-blogs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<BlogSummaryResponse>> getMyBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        
        try {
            Page<Blog> userBlogs = blogService.getBlogsByUserEmail(userEmail, pageable);
            Page<BlogSummaryResponse> response = userBlogs.map(this::convertToBlogSummaryResponse);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }
    
    /**
     * Check if current user is the author of a blog
     * GET /api/blogs/{id}/is-author
     */
    @GetMapping("/{id}/is-author")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Boolean> isAuthor(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        boolean isAuthor = blogService.isAuthor(id, userEmail);
        
        return ResponseEntity.ok(isAuthor);
    }
    
    // Helper methods to convert entities to DTOs
    
    private BlogResponse convertToBlogResponse(Blog blog) {
        BlogResponse.AuthorInfo authorInfo = new BlogResponse.AuthorInfo(
            blog.getAuthor().getId(),
            blog.getAuthor().getEmail(),
            blog.getAuthor().getFirstName(),
            blog.getAuthor().getLastName()
        );
        
        return new BlogResponse(
            blog.getId(),
            blog.getTitle(),
            blog.getContent(),
            java.util.Collections.emptyList(), // No tags
            blog.getViewCount(),
            authorInfo,
            blog.getCreatedAt(),
            blog.getUpdatedAt()
        );
    }
    
    private BlogSummaryResponse convertToBlogSummaryResponse(Blog blog) {
        BlogSummaryResponse.AuthorInfo authorInfo = new BlogSummaryResponse.AuthorInfo(
            blog.getAuthor().getId(),
            blog.getAuthor().getEmail(),
            blog.getAuthor().getFirstName(),
            blog.getAuthor().getLastName()
        );
        
        // Create content preview (first 200 characters)
        String contentPreview = blog.getContent().length() > 200 
            ? blog.getContent().substring(0, 200) + "..."
            : blog.getContent();
        
        return new BlogSummaryResponse(
            blog.getId(),
            blog.getTitle(),
            contentPreview,
            java.util.Collections.emptyList(), // No tags
            blog.getViewCount(),
            authorInfo,
            blog.getCreatedAt(),
            blog.getUpdatedAt()
        );
    }
}
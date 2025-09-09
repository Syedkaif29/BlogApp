package com.blogapp.blogapp.service;

import com.blogapp.blogapp.entity.Blog;
import com.blogapp.blogapp.entity.User;
import com.blogapp.blogapp.repository.BlogRepository;
import com.blogapp.blogapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {
    
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all blogs with pagination (public access)
     * Returns blogs ordered by creation date descending (newest first)
     */
    @Transactional(readOnly = true)
    public Page<Blog> getAllBlogs(Pageable pageable) {
        return blogRepository.findAllOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * Get a specific blog by ID (public access)
     */
    @Transactional(readOnly = true)
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }
    
    /**
     * Get all blogs by a specific author with pagination
     */
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByAuthor(User author, Pageable pageable) {
        return blogRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }
    
    /**
     * Get all blogs by author ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByAuthorId(Long authorId, Pageable pageable) {
        return blogRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }
    
    /**
     * Create a new blog post
     * Only authenticated users can create blogs
     */
    public Blog createBlog(String title, String content, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + authorEmail));
        
        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setAuthor(author);
        
        return blogRepository.save(blog);
    }
    
    /**
     * Update an existing blog post
     * Only the author can update their own blog
     */
    public Blog updateBlog(Long blogId, String title, String content, String currentUserEmail) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        
        // Authorization check: only the author can update their blog
        if (!blog.getAuthor().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You can only update your own blog posts");
        }
        
        blog.setTitle(title);
        blog.setContent(content);
        
        return blogRepository.save(blog);
    }
    
    /**
     * Delete a blog post
     * Only the author can delete their own blog
     */
    public void deleteBlog(Long blogId, String currentUserEmail) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        
        // Authorization check: only the author can delete their blog
        if (!blog.getAuthor().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You can only delete your own blog posts");
        }
        
        blogRepository.delete(blog);
    }
    
    /**
     * Check if a user is the author of a specific blog
     */
    @Transactional(readOnly = true)
    public boolean isAuthor(Long blogId, String userEmail) {
        Optional<Blog> blog = blogRepository.findById(blogId);
        return blog.isPresent() && blog.get().getAuthor().getEmail().equals(userEmail);
    }
    
    /**
     * Get total count of blogs by author
     */
    @Transactional(readOnly = true)
    public long getBlogCountByAuthor(User author) {
        return blogRepository.countByAuthor(author);
    }
    
    /**
     * Get total count of blogs by author ID
     */
    @Transactional(readOnly = true)
    public long getBlogCountByAuthorId(Long authorId) {
        return blogRepository.countByAuthorId(authorId);
    }
    
    /**
     * Get all blogs by user email with pagination
     */
    @Transactional(readOnly = true)
    public Page<Blog> getBlogsByUserEmail(String userEmail, Pageable pageable) {
        User author = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        
        return blogRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }
}
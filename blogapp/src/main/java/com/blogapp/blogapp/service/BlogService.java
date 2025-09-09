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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogService {
    
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    
    /**
     * Get all blogs with pagination and sorting options
     */
    @Transactional(readOnly = true)
    public Page<Blog> getAllBlogs(Pageable pageable, String sortBy) {
        switch (sortBy != null ? sortBy.toLowerCase() : "date") {
            case "popularity":
                return blogRepository.findAllOrderByViewCountDesc(pageable);
            case "title":
                return blogRepository.findAllOrderByTitleAsc(pageable);
            case "date":
            default:
                return blogRepository.findAllOrderByCreatedAtDesc(pageable);
        }
    }
    
    // Tag-related methods removed
    
    /**
     * Search blogs by title or content
     */
    @Transactional(readOnly = true)
    public Page<Blog> searchBlogs(String searchTerm, Pageable pageable) {
        return blogRepository.searchByTitleOrContent(searchTerm, pageable);
    }
    
    // Tag search methods removed
    
    /**
     * Get a specific blog by ID (public access)
     */
    @Transactional(readOnly = true)
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findByIdWithAuthor(id);
    }
    
    /**
     * Get a specific blog by ID and increment view count
     */
    public Optional<Blog> getBlogByIdAndIncrementViews(Long id) {
        Optional<Blog> blogOpt = blogRepository.findByIdWithAuthor(id);
        if (blogOpt.isPresent()) {
            blogRepository.incrementViewCount(id);
            // Refresh the blog to get updated view count
            return blogRepository.findByIdWithAuthor(id);
        }
        return blogOpt;
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
        blog.setViewCount(0L);
        blog.setAuthor(author);
        
        return blogRepository.save(blog);
    }
    
    /**
     * Update an existing blog post
     * Only the author can update their own blog
     */
    public Blog updateBlog(Long blogId, String title, String content, String currentUserEmail) {
        Blog blog = blogRepository.findByIdWithAuthor(blogId)
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
        Blog blog = blogRepository.findByIdWithAuthor(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found with id: " + blogId));
        
        // Authorization check: only the author can delete their blog
        if (!blog.getAuthor().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You can only delete your own blog posts");
        }
        
        // Delete the blog directly - no tag relationships to clear
        blogRepository.delete(blog);
    }
    
    /**
     * Check if a user is the author of a specific blog
     */
    @Transactional(readOnly = true)
    public boolean isAuthor(Long blogId, String userEmail) {
        Optional<Blog> blog = blogRepository.findByIdWithAuthor(blogId);
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
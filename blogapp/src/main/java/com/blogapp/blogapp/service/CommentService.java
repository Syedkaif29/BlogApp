package com.blogapp.blogapp.service;

import com.blogapp.blogapp.dto.CommentRequest;
import com.blogapp.blogapp.dto.CommentResponse;
import com.blogapp.blogapp.entity.Blog;
import com.blogapp.blogapp.entity.Comment;
import com.blogapp.blogapp.entity.User;
import com.blogapp.blogapp.repository.BlogRepository;
import com.blogapp.blogapp.repository.CommentRepository;
import com.blogapp.blogapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private BlogRepository blogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Page<CommentResponse> getCommentsByBlogId(Long blogId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId, pageable);
        return comments.map(this::convertToResponse);
    }
    
    public List<CommentResponse> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public CommentResponse createComment(Long blogId, CommentRequest request, String userEmail) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setBlog(blog);
        comment.setAuthor(user);
        comment.setIsEdited(false);
        
        Comment savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }
    
    public CommentResponse updateComment(Long commentId, CommentRequest request, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own comments");
        }
        
        comment.setContent(request.getContent());
        comment.setIsEdited(true);
        
        Comment updatedComment = commentRepository.save(comment);
        return convertToResponse(updatedComment);
    }
    
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }
        
        commentRepository.delete(comment);
    }
    
    public long getCommentCountByBlogId(Long blogId) {
        return commentRepository.countByBlogId(blogId);
    }
    
    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setBlogId(comment.getBlog().getId());
        response.setAuthorId(comment.getAuthor().getId());
        response.setAuthorName(comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName());
        response.setIsEdited(comment.getIsEdited());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        return response;
    }
}
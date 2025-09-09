package com.blogapp.blogapp.repository;

import com.blogapp.blogapp.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Page<Comment> findByBlogIdOrderByCreatedAtDesc(Long blogId, Pageable pageable);
    
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    
    long countByBlogId(Long blogId);
    
    @Query("SELECT c FROM Comment c WHERE c.blog.id = :blogId ORDER BY c.createdAt ASC")
    Page<Comment> findByBlogIdOrderByCreatedAtAsc(@Param("blogId") Long blogId, Pageable pageable);
}
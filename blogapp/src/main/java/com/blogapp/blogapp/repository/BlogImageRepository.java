package com.blogapp.blogapp.repository;

import com.blogapp.blogapp.entity.BlogImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogImageRepository extends JpaRepository<BlogImage, Long> {
    
    List<BlogImage> findByBlogId(Long blogId);
    
    void deleteByBlogId(Long blogId);
}
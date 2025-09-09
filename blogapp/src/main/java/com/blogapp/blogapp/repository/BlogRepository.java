package com.blogapp.blogapp.repository;

import com.blogapp.blogapp.entity.Blog;
import com.blogapp.blogapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    /**
     * Find all blogs ordered by creation date descending (newest first) with pagination
     */
    @Query("SELECT b FROM Blog b ORDER BY b.createdAt DESC")
    Page<Blog> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find all blogs by a specific author ordered by creation date descending
     */
    @Query("SELECT b FROM Blog b WHERE b.author = :author ORDER BY b.createdAt DESC")
    Page<Blog> findByAuthorOrderByCreatedAtDesc(@Param("author") User author, Pageable pageable);
    
    /**
     * Find all blogs by author ID ordered by creation date descending
     */
    @Query("SELECT b FROM Blog b WHERE b.author.id = :authorId ORDER BY b.createdAt DESC")
    Page<Blog> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId, Pageable pageable);
    
    /**
     * Count total blogs by author
     */
    long countByAuthor(User author);
    
    /**
     * Count total blogs by author ID
     */
    long countByAuthorId(Long authorId);
}
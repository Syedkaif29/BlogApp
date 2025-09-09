package com.blogapp.blogapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.blogapp.blogapp.entity.Blog;
import com.blogapp.blogapp.entity.User;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    
    /**
     * Find all blogs ordered by creation date descending (newest first) with pagination
     */
    @Query("SELECT DISTINCT b FROM Blog b LEFT JOIN FETCH b.author ORDER BY b.createdAt DESC")
    Page<Blog> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * Find all blogs ordered by view count descending (most popular first)
     */
    @Query("SELECT DISTINCT b FROM Blog b LEFT JOIN FETCH b.author ORDER BY b.viewCount DESC, b.createdAt DESC")
    Page<Blog> findAllOrderByViewCountDesc(Pageable pageable);
    
    /**
     * Find all blogs ordered by title alphabetically
     */
    @Query("SELECT DISTINCT b FROM Blog b LEFT JOIN FETCH b.author ORDER BY b.title ASC")
    Page<Blog> findAllOrderByTitleAsc(Pageable pageable);
    
    /**
     * Find all blogs by a specific author ordered by creation date descending
     */
    @Query("SELECT DISTINCT b FROM Blog b WHERE b.author = :author ORDER BY b.createdAt DESC")
    Page<Blog> findByAuthorOrderByCreatedAtDesc(@Param("author") User author, Pageable pageable);
    
    /**
     * Find all blogs by author ID ordered by creation date descending
     */
    @Query("SELECT DISTINCT b FROM Blog b LEFT JOIN FETCH b.author WHERE b.author.id = :authorId ORDER BY b.createdAt DESC")
    Page<Blog> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId, Pageable pageable);
    
    /**
     * Count total blogs by author
     */
    long countByAuthor(User author);
    
    /**
     * Count total blogs by author ID
     */
    long countByAuthorId(Long authorId);
    
    // Tag-related queries removed
    
    /**
     * Search blogs by title or content
     */
    @Query("SELECT DISTINCT b FROM Blog b LEFT JOIN FETCH b.author WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(b.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Blog> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Find blog by ID with author eagerly loaded
     */
    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.author WHERE b.id = :id")
    java.util.Optional<Blog> findByIdWithAuthor(@Param("id") Long id);
    
    /**
     * Increment view count for a blog
     */
    @Modifying
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :blogId")
    void incrementViewCount(@Param("blogId") Long blogId);
}
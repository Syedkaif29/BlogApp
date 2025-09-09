package com.blogapp.blogapp.service;

import com.blogapp.blogapp.dto.ImageUploadResponse;
import com.blogapp.blogapp.entity.Blog;
import com.blogapp.blogapp.entity.BlogImage;
import com.blogapp.blogapp.entity.User;
import com.blogapp.blogapp.repository.BlogImageRepository;
import com.blogapp.blogapp.repository.BlogRepository;
import com.blogapp.blogapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    
    @Autowired
    private BlogImageRepository blogImageRepository;
    
    @Autowired
    private BlogRepository blogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.upload.max-file-size:5242880}") // 5MB default
    private long maxFileSize;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    public ImageUploadResponse uploadImage(Long blogId, MultipartFile file, String userEmail) {
        // Validate file
        validateFile(file);
        
        // Check if user owns the blog
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!blog.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only upload images to your own blogs");
        }
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Save metadata to database
            BlogImage blogImage = new BlogImage();
            blogImage.setFileName(uniqueFilename);
            blogImage.setOriginalName(originalFilename);
            blogImage.setFilePath(filePath.toString());
            blogImage.setContentType(file.getContentType());
            blogImage.setFileSize(file.getSize());
            blogImage.setBlog(blog);
            
            BlogImage savedImage = blogImageRepository.save(blogImage);
            
            return convertToUploadResponse(savedImage);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }
    
    public void deleteImage(Long imageId, String userEmail) {
        BlogImage image = blogImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!image.getBlog().getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete images from your own blogs");
        }
        
        try {
            // Delete file from disk
            Path filePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Delete metadata from database
            blogImageRepository.delete(image);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }
    
    public List<BlogImage> getImagesByBlogId(Long blogId) {
        return blogImageRepository.findByBlogId(blogId);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("File type not allowed. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
    
    private ImageUploadResponse convertToUploadResponse(BlogImage image) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setId(image.getId());
        response.setFileName(image.getFileName());
        response.setOriginalName(image.getOriginalName());
        response.setUrl("/api/images/" + image.getFileName());
        response.setContentType(image.getContentType());
        response.setFileSize(image.getFileSize());
        return response;
    }
}
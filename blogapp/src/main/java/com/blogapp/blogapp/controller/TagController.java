package com.blogapp.blogapp.controller;

import com.blogapp.blogapp.dto.TagRequest;
import com.blogapp.blogapp.dto.TagResponse;
import com.blogapp.blogapp.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getPopularTags() {
        List<TagResponse> popularTags = tagService.getPopularTags();
        return ResponseEntity.ok(popularTags);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> searchTags(@RequestParam String name) {
        List<TagResponse> tags = tagService.searchTags(name);
        return ResponseEntity.ok(tags);
    }
    
    @PostMapping
    public ResponseEntity<?> createTag(@Valid @RequestBody TagRequest request) {
        try {
            TagResponse response = tagService.createTag(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
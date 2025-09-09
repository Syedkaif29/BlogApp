package com.blogapp.blogapp.service;

import com.blogapp.blogapp.dto.TagRequest;
import com.blogapp.blogapp.dto.TagResponse;
import com.blogapp.blogapp.entity.Tag;
import com.blogapp.blogapp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TagResponse> getPopularTags() {
        return tagRepository.findPopularTags().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TagResponse> searchTags(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public TagResponse createTag(TagRequest request) {
        if (tagRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tag with name '" + request.getName() + "' already exists");
        }
        
        Tag tag = new Tag();
        tag.setName(request.getName().toLowerCase().trim());
        tag.setColor(request.getColor());
        
        Tag savedTag = tagRepository.save(tag);
        return convertToResponse(savedTag);
    }
    
    public Optional<Tag> findByName(String name) {
        return tagRepository.findByName(name.toLowerCase().trim());
    }
    
    public Tag findOrCreateTag(String name) {
        return findByName(name)
                .orElseGet(() -> {
                    Tag newTag = new Tag(name.toLowerCase().trim());
                    return tagRepository.save(newTag);
                });
    }
    
    private TagResponse convertToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setColor(tag.getColor());
        response.setCreatedAt(tag.getCreatedAt());
        response.setUsageCount((long) tag.getBlogs().size());
        return response;
    }
}
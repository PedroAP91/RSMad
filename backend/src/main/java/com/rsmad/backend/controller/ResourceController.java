package com.rsmad.backend.controller;

import java.net.URI;
import java.util.List;

import com.rsmad.backend.dto.ResourceDTO;
import com.rsmad.backend.dto.ResourceRequest;
import com.rsmad.backend.mapper.ResourceMapper;
import com.rsmad.backend.model.Resource;
import com.rsmad.backend.service.ResourceService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;

    public ResourceController(ResourceService resourceService, ResourceMapper resourceMapper) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
    }

    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(@Valid @RequestBody ResourceRequest request) {
        Resource created = resourceService.create(resourceMapper.toModel(request));
        ResourceDTO response = resourceMapper.toDto(created);
        return ResponseEntity.created(URI.create("/api/resources/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDTO> updateResource(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        Resource updated = resourceService.update(id, resourceMapper.toModel(request));
        return ResponseEntity.ok(resourceMapper.toDto(updated));
    }

    @GetMapping
    public List<ResourceDTO> getResources() {
        return resourceService.findAll().stream()
                .map(resourceMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResourceDTO getResourceById(@PathVariable Long id) {
        return resourceMapper.toDto(resourceService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

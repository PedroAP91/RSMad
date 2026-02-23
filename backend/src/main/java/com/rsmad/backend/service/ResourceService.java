package com.rsmad.backend.service;

import java.util.List;

import com.rsmad.backend.exception.ResourceNotFoundException;
import com.rsmad.backend.model.Resource;
import com.rsmad.backend.repository.ResourceRepository;

import org.springframework.stereotype.Service;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public Resource create(Resource resource) {
        return resourceRepository.create(resource);
    }

    public List<Resource> findAll() {
        return resourceRepository.findAllOrderedById();
    }

    public Resource findById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public Resource update(Long id, Resource resource) {
        return resourceRepository.update(id, resource)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public void delete(Long id) {
        resourceRepository.deleteById(id);
    }
}

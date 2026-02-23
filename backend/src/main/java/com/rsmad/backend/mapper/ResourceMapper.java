package com.rsmad.backend.mapper;

import com.rsmad.backend.dto.ResourceDTO;
import com.rsmad.backend.dto.ResourceRequest;
import com.rsmad.backend.model.Resource;

import org.springframework.stereotype.Component;

@Component
public class ResourceMapper {

    public Resource toModel(ResourceRequest request) {
        return new Resource(null, request.getNombre());
    }

    public ResourceDTO toDto(Resource model) {
        ResourceDTO dto = new ResourceDTO();
        dto.setId(model.id());
        dto.setNombre(model.nombre());
        return dto;
    }
}

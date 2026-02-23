package com.rsmad.backend.mapper;

import com.rsmad.backend.dto.RequestDTO;
import com.rsmad.backend.dto.RequestRequest;
import com.rsmad.backend.model.Request;

import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public Request toModel(RequestRequest request) {
        return new Request(
                null,
                request.getTitulo(),
                request.getDescripcion(),
                request.getContactPhone(),
                request.getDistrict(),
                request.getNotes(),
                request.getTipo(),
                null,
                null,
                null
        );
    }

    public RequestDTO toDto(Request model) {
        RequestDTO dto = new RequestDTO();
        dto.setId(model.id());
        dto.setTitulo(model.titulo());
        dto.setDescripcion(model.descripcion());
        dto.setContactPhone(model.contactPhone());
        dto.setDistrict(model.district());
        dto.setNotes(model.notes());
        dto.setTipo(model.tipo());
        dto.setEstado(model.estado());
        dto.setCreatedAt(model.createdAt());
        dto.setUpdatedAt(model.updatedAt());
        return dto;
    }
}

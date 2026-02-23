package com.rsmad.backend.controller;

import java.util.List;

import com.rsmad.backend.dto.PagedResponse;
import com.rsmad.backend.dto.RequestDTO;
import com.rsmad.backend.mapper.RequestMapper;
import com.rsmad.backend.model.Request;
import com.rsmad.backend.model.RequestStatus;
import com.rsmad.backend.model.RequestType;
import com.rsmad.backend.service.RequestService;
import com.rsmad.backend.service.ResourceService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
@Validated
public class ResourceRequestsController {

    private final ResourceService resourceService;
    private final RequestService requestService;
    private final RequestMapper requestMapper;

    public ResourceRequestsController(
            ResourceService resourceService,
            RequestService requestService,
            RequestMapper requestMapper
    ) {
        this.resourceService = resourceService;
        this.requestService = requestService;
        this.requestMapper = requestMapper;
    }

    @GetMapping("/{id}/requests")
    public PagedResponse<RequestDTO> getRequestsByResource(
            @PathVariable Long id,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestType type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        resourceService.findById(id);
        PagedResponse<Request> paged = requestService.findAllPaged(status, type, q, id, true, sort, page, size);
        List<RequestDTO> items = paged.getItems().stream()
                .map(requestMapper::toDto)
                .toList();
        return new PagedResponse<>(items, paged.getPage(), paged.getSize(), paged.getTotal());
    }
}

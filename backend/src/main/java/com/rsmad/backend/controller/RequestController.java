package com.rsmad.backend.controller;

import java.net.URI;
import java.util.List;

import com.rsmad.backend.dto.PagedResponse;
import com.rsmad.backend.dto.RequestDTO;
import com.rsmad.backend.dto.RequestRequest;
import com.rsmad.backend.dto.AssignResourceRequest;
import com.rsmad.backend.dto.UpdateRequestStatusRequest;
import com.rsmad.backend.mapper.RequestMapper;
import com.rsmad.backend.model.Request;
import com.rsmad.backend.model.RequestStatus;
import com.rsmad.backend.model.RequestType;
import com.rsmad.backend.service.RequestService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@Validated
public class RequestController {

    private final RequestService requestService;
    private final RequestMapper requestMapper;

    public RequestController(RequestService requestService, RequestMapper requestMapper) {
        this.requestService = requestService;
        this.requestMapper = requestMapper;
    }

    @PostMapping
    public ResponseEntity<RequestDTO> createRequest(@Valid @RequestBody RequestRequest request) {
        Request created = requestService.create(requestMapper.toModel(request));
        RequestDTO response = requestMapper.toDto(created);
        return ResponseEntity.created(URI.create("/api/requests/" + response.getId())).body(response);
    }

    @GetMapping
    public PagedResponse<RequestDTO> getRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestType type,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @Positive Long resourceId,
            @RequestParam(required = false) Boolean assigned,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        PagedResponse<Request> paged = requestService.findAllPaged(status, type, q, resourceId, assigned, sort, page, size);
        List<RequestDTO> items = paged.getItems().stream()
                .map(requestMapper::toDto)
                .toList();
        return new PagedResponse<>(items, paged.getPage(), paged.getSize(), paged.getTotal());
    }

    @GetMapping("/{id}")
    public RequestDTO getRequestById(@PathVariable Long id) {
        return requestMapper.toDto(requestService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestDTO> updateRequest(@PathVariable Long id, @Valid @RequestBody RequestRequest request) {
        Request updated = requestService.update(id, requestMapper.toModel(request));
        return ResponseEntity.ok(requestMapper.toDto(updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RequestDTO> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusRequest request
    ) {
        Request updated = requestService.updateStatus(id, request.getEstado());
        return ResponseEntity.ok(requestMapper.toDto(updated));
    }

    @PatchMapping("/{id}/assign-resource")
    public ResponseEntity<RequestDTO> assignResource(
            @PathVariable Long id,
            @Valid @RequestBody AssignResourceRequest request
    ) {
        Request updated = requestService.assignResource(id, request.getResourceId());
        return ResponseEntity.ok(requestMapper.toDto(updated));
    }

    @PatchMapping("/{id}/unassign-resource")
    public ResponseEntity<RequestDTO> unassignResource(@PathVariable Long id) {
        Request updated = requestService.unassignResource(id);
        return ResponseEntity.ok(requestMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        requestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

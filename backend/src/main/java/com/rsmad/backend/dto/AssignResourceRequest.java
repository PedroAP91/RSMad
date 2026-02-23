package com.rsmad.backend.dto;

import jakarta.validation.constraints.NotNull;

public class AssignResourceRequest {

    @NotNull(message = "resourceId es obligatorio")
    private Long resourceId;

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}

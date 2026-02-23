package com.rsmad.backend.dto;

import com.rsmad.backend.model.RequestStatus;

import jakarta.validation.constraints.NotNull;

public class UpdateRequestStatusRequest {

    @NotNull(message = "estado es obligatorio")
    private RequestStatus estado;

    public RequestStatus getEstado() {
        return estado;
    }

    public void setEstado(RequestStatus estado) {
        this.estado = estado;
    }
}

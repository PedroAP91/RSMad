package com.rsmad.backend.dto;

import java.time.Instant;

import com.rsmad.backend.model.RequestStatus;
import com.rsmad.backend.model.RequestType;

public class RequestDTO {

    private Long id;
    private String titulo;
    private String descripcion;
    private RequestType tipo;
    private RequestStatus estado;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public RequestType getTipo() {
        return tipo;
    }

    public void setTipo(RequestType tipo) {
        this.tipo = tipo;
    }

    public RequestStatus getEstado() {
        return estado;
    }

    public void setEstado(RequestStatus estado) {
        this.estado = estado;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

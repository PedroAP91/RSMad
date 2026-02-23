package com.rsmad.backend.dto;

import com.rsmad.backend.model.RequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RequestRequest {

    @NotBlank(message = "titulo es obligatorio")
    private String titulo;

    @NotNull(message = "tipo es obligatorio")
    private RequestType tipo;

    private String descripcion;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public RequestType getTipo() {
        return tipo;
    }

    public void setTipo(RequestType tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

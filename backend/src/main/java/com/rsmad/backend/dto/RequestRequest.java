package com.rsmad.backend.dto;

import com.rsmad.backend.model.RequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RequestRequest {

    @NotBlank(message = "titulo es obligatorio")
    private String titulo;

    @NotNull(message = "tipo es obligatorio")
    private RequestType tipo;

    private String descripcion;

    @Pattern(regexp = "^\\+?\\d{9,15}$", message = "telefono invalido")
    private String contactPhone;

    @Size(max = 80, message = "district demasiado largo")
    private String district;

    @Size(max = 500, message = "notes demasiado largo")
    private String notes;

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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        if (contactPhone == null) {
            this.contactPhone = null;
            return;
        }
        String normalized = contactPhone.replaceAll("\\s+", "");
        this.contactPhone = normalized.isBlank() ? null : normalized;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

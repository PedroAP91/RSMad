package com.rsmad.backend.model;

import java.time.Instant;

public record Request(
        Long id,
        String titulo,
        String descripcion,
        RequestType tipo,
        RequestStatus estado,
        Instant createdAt,
        Instant updatedAt
) {}

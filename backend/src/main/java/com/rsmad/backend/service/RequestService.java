package com.rsmad.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.rsmad.backend.dto.PagedResponse;
import com.rsmad.backend.exception.RequestNotFoundException;
import com.rsmad.backend.model.Request;
import com.rsmad.backend.model.RequestStatus;
import com.rsmad.backend.model.RequestType;
import com.rsmad.backend.repository.RequestRepository;

import org.springframework.stereotype.Service;

@Service
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public Request create(Request request) {
        return requestRepository.create(request);
    }

    public List<Request> findAll() {
        return requestRepository.findAllOrderedById();
    }

    public List<Request> findAll(RequestStatus status, RequestType type) {
        return requestRepository.findAllOrderedById().stream()
                .filter(request -> status == null || request.estado() == status)
                .filter(request -> type == null || request.tipo() == type)
                .toList();
    }

    public PagedResponse<Request> findAllPaged(RequestStatus status, RequestType type, String q, String sort, int page, int size) {
        Comparator<Request> comparator = buildSortComparator(sort);
        String query = q == null ? null : q.trim();
        boolean hasQuery = query != null && !query.isBlank();
        String normalizedQuery = hasQuery ? query.toLowerCase() : null;

        List<Request> filtered = requestRepository.findAllOrderedById().stream()
                .filter(request -> status == null || request.estado() == status)
                .filter(request -> type == null || request.tipo() == type)
                .filter(request -> {
                    if (!hasQuery) {
                        return true;
                    }
                    boolean matchesTitulo = request.titulo() != null
                            && request.titulo().toLowerCase().contains(normalizedQuery);
                    boolean matchesDescripcion = request.descripcion() != null
                            && request.descripcion().toLowerCase().contains(normalizedQuery);
                    return matchesTitulo || matchesDescripcion;
                })
                .sorted(comparator)
                .toList();

        int total = filtered.size();
        int from = page * size;
        if (from >= total) {
            return new PagedResponse<>(List.of(), page, size, total);
        }

        int to = Math.min(from + size, total);
        return new PagedResponse<>(filtered.subList(from, to), page, size, total);
    }

    private Comparator<Request> buildSortComparator(String sort) {
        String effectiveSort = sort == null || sort.isBlank() ? "id,asc" : sort.trim();
        String[] parts = effectiveSort.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid sort format");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase(Locale.ROOT);

        Comparator<Request> comparator = switch (field) {
            case "id" -> Comparator.comparing(Request::id);
            case "titulo" -> Comparator.comparing(
                    request -> request.titulo() == null ? "" : request.titulo().toLowerCase(Locale.ROOT)
            );
            case "createdAt" -> Comparator.comparing(Request::createdAt);
            case "updatedAt" -> Comparator.comparing(Request::updatedAt);
            default -> throw new IllegalArgumentException("Invalid sort field");
        };

        return switch (direction) {
            case "asc" -> comparator;
            case "desc" -> comparator.reversed();
            default -> throw new IllegalArgumentException("Invalid sort direction");
        };
    }

    public Request findById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    public Request update(Long id, Request request) {
        return requestRepository.update(id, request)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    public Request updateStatus(Long id, RequestStatus estado) {
        return requestRepository.updateStatus(id, estado)
                .orElseThrow(() -> new RequestNotFoundException(id));
    }

    public void delete(Long id) {
        requestRepository.deleteById(id);
    }
}

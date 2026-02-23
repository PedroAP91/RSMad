package com.rsmad.backend.repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.rsmad.backend.model.Request;
import com.rsmad.backend.model.RequestStatus;

import org.springframework.stereotype.Repository;

@Repository
public class RequestRepository {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, Request> store = new ConcurrentHashMap<>();

    public Request create(Request request) {
        long id = idGenerator.incrementAndGet();
        Instant now = Instant.now();
        Request created = new Request(
                id,
                request.titulo(),
                request.descripcion(),
                request.contactPhone(),
                request.district(),
                request.notes(),
                request.tipo(),
                RequestStatus.ABIERTA,
                now,
                now
        );
        store.put(id, created);
        return created;
    }

    public List<Request> findAllOrderedById() {
        return store.values().stream()
                .sorted(Comparator.comparing(Request::id))
                .toList();
    }

    public Optional<Request> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Request> update(Long id, Request request) {
        Request updated = store.computeIfPresent(
                id,
                (key, existing) -> new Request(
                        existing.id(),
                        request.titulo(),
                        request.descripcion(),
                        request.contactPhone(),
                        request.district(),
                        request.notes(),
                        request.tipo(),
                        existing.estado(),
                        existing.createdAt(),
                        Instant.now()
                )
        );
        return Optional.ofNullable(updated);
    }

    public Optional<Request> updateStatus(Long id, RequestStatus estado) {
        Request updated = store.computeIfPresent(
                id,
                (key, existing) -> new Request(
                        existing.id(),
                        existing.titulo(),
                        existing.descripcion(),
                        existing.contactPhone(),
                        existing.district(),
                        existing.notes(),
                        existing.tipo(),
                        estado,
                        existing.createdAt(),
                        Instant.now()
                )
        );
        return Optional.ofNullable(updated);
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public void clear() {
        store.clear();
        idGenerator.set(0);
    }
}

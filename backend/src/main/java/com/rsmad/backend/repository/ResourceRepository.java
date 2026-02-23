package com.rsmad.backend.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.rsmad.backend.model.Resource;

import org.springframework.stereotype.Repository;

@Repository
public class ResourceRepository {

    private final AtomicLong idGenerator = new AtomicLong(0);
    private final Map<Long, Resource> store = new ConcurrentHashMap<>();

    public Resource create(Resource resource) {
        long id = idGenerator.incrementAndGet();
        Resource created = new Resource(id, resource.nombre());
        store.put(id, created);
        return created;
    }

    public List<Resource> findAllOrderedById() {
        return store.values().stream()
                .sorted(Comparator.comparing(Resource::id))
                .toList();
    }

    public Optional<Resource> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Resource> update(Long id, Resource resource) {
        Resource updated = store.computeIfPresent(
                id,
                (key, existing) -> new Resource(id, resource.nombre())
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

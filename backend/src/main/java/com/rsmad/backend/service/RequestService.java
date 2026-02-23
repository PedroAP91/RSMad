package com.rsmad.backend.service;

import java.util.List;

import com.rsmad.backend.exception.RequestNotFoundException;
import com.rsmad.backend.model.Request;
import com.rsmad.backend.model.RequestStatus;
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

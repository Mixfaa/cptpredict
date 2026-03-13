package com.mixfa.cptpredict.service.repo;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.repository.ObjectRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LocalStoreRepo<T, ID> implements CustomizableRepo<T, ID> {
    private final Class<T> typeClass;
    private final ObjectRepository<T> repository;

    public LocalStoreRepo(Class<T> typeClass, Nitrite database) {
        this.typeClass = typeClass;
        this.repository = database.getRepository(typeClass);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(repository.getById(id));
    }

    @Override
    public List<T> findAll() {
        return repository.find().toList();
    }

    @Override
    public void delete(T obj) {
        repository.remove(obj);
    }

    @Override
    public T save(T obj) {
        repository.insert(obj);
        return obj;
    }

    @Override
    public Class<T> getEntityClass() {
        return typeClass;
    }

    @Override
    public void close() throws IOException {
        repository.close();
    }
}

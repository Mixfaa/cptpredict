package com.mixfa.cptpredict.service.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MongoDbRepo<T, ID> implements CustomizableRepo<T, ID> {
    private final Class<T> typeClass;
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(mongoTemplate.findById(id, typeClass));
    }

    @Override
    public List<T> findAll() {
        return mongoTemplate.findAll(typeClass);
    }

    @Override
    public void delete(T obj) {
        mongoTemplate.remove(obj);
    }

    @Override
    public T save(T obj) {
        return mongoTemplate.save(obj);
    }

    @Override
    public Class<T> getEntityClass() {
        return typeClass;
    }

    @Override
    public void close() throws IOException {

    }
}

package com.mixfa.cptpredict.service.repo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CustomizableRepo<T, ID> extends Closeable {
    Optional<T> findById(ID id);

    List<T> findAll();

    void delete(T obj);

    T save(T obj);

    Class<T> getEntityClass();

    default boolean isStub() {
        return false;
    }

    @Setter
    @Getter
    @AllArgsConstructor
    class Proxy<T, ID> implements CustomizableRepo<T, ID> {
        @Delegate
        private volatile CustomizableRepo<T, ID> implementation;

        public void setImpl(CustomizableRepo<?, ?> customizableRepo) {
            if (!this.implementation.getEntityClass().equals(customizableRepo.getClass()))
                throw new IllegalArgumentException("CustomizableRepo has different entity class");
            this.implementation = (CustomizableRepo<T, ID>) customizableRepo;
        }
    }

    class Stub<T, ID> implements CustomizableRepo<T, ID> {
        private static final Stub INSTANCE = new Stub();

        public static <T, ID> Stub<T, ID> getInstance() {
            return INSTANCE;
        }

        @Override
        public boolean isStub() {
            return true;
        }

        @Override
        public Optional<T> findById(ID id) {
            return Optional.empty();
        }

        @Override
        public List<T> findAll() {
            return List.of();
        }

        @Override
        public void delete(T obj) {

        }

        @Override
        public T save(T obj) {
            return obj;
        }

        @Override
        public Class<T> getEntityClass() {
            return null;
        }

        @Override
        public void close() throws IOException {

        }
    }
}

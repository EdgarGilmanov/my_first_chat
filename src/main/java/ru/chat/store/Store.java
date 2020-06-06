package ru.chat.store;

import java.util.Collection;
import java.util.Optional;

public interface Store<T> {
    T save(T t);

    boolean delete(T t);

    Collection<T> getAll();

    T update(T t);

    Optional<T> findBy(String param);

    Optional<T> findById(String id);
}

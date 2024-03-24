package com.fullstackprojectbackend.securecapita.repository;

import com.fullstackprojectbackend.securecapita.domain.User;

import java.util.Collection;

public interface UserRepository <T extends User>{
    // crud operations

    T create(T data);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    Boolean delete(Long id);
    T update(T data);
}

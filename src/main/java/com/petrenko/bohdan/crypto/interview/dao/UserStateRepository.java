package com.petrenko.bohdan.crypto.interview.dao;

import com.petrenko.bohdan.crypto.interview.dao.entity.UserStateEntity;

import org.springframework.data.repository.CrudRepository;

public interface UserStateRepository extends CrudRepository<UserStateEntity, Long> {
}

package io.zeebe.tasklist.repository;

import io.zeebe.tasklist.entity.HazelcastConfig;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HazelcastConfigRepository extends CrudRepository<HazelcastConfig, String> {}

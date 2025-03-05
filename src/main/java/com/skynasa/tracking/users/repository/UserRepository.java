package com.skynasa.tracking.users.repository;

import com.skynasa.tracking.users.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    List<User> getByParentId(UUID parentId);

    Page<User> getByParentId(UUID parentId, Pageable pageable);

    Optional<User> findByKeycloakUserId(String keycloakUserId);

}

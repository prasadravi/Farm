package com.naturalmilk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naturalmilk.model.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findFirstByOrderByCreatedAtAsc();
}

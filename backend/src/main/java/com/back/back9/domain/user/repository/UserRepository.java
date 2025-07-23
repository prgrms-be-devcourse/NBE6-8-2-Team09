package com.back.back9.domain.user.repository;

import com.back.back9.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserLoginId(String userLoginId);
    boolean existsByUsername(String username);
    boolean existsByUserLoginId(String userLoginId);
}
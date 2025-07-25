package com.back.back9.domain.user.repository;

import com.back.back9.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByApiKey(String apiKey);
    Optional<User> findByUserLoginId(String userLoginId);
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContaining(String keyword);
}

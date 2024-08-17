package com.box_chat.identity_service.repository;

import com.box_chat.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u JOIN Account a WHERE a.email = :email")
    Optional<User> findUserByEmail(@PathVariable("email") String email);
}

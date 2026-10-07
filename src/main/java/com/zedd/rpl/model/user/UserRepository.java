package com.zedd.rpl.model.user;

import com.zedd.rpl.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByEmailIgnoreCase(String email);

  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByPhoneNumber(String phoneNumber);

  boolean existsByRole(Role role);
}

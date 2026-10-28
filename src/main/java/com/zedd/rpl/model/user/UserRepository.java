package com.zedd.rpl.model.user;

import com.zedd.rpl.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByRole(Role role);

  @EntityGraph(attributePaths = "student")
  Optional<User> findByStudentStudentNumber(String nim);

  @EntityGraph(attributePaths = "student")
  Optional<User> findByUsername(String username);

  Optional<User> findByStudentId(Long studentId);

  List<User> findByStudentIdIn(Collection<Long> studentIds);

  boolean existsByUsername(String username);

  boolean existsByUsernameAndIdNot(String username, Long id);

  boolean existsByStudentId(Long studentId);

  void deleteByStudentId(Long studentId);
}

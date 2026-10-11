package com.zedd.rpl.model.room;


import com.zedd.rpl.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

  List<Room> findAllByOrderByName();

  List<Room> findByActiveTrueOrderByName();

  List<Room> findByTypeAndActiveTrueOrderByName(RoomType type);

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);

  long countByActiveTrue();
}


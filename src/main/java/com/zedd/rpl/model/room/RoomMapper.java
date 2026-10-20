package com.zedd.rpl.model.room;


import com.zedd.rpl.model.room.req.RoomRequest;
import com.zedd.rpl.model.room.res.RoomResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RoomMapper {

  public RoomResponse toResponse(Room r) {
    return new RoomResponse(r.getId(), r.getName(), r.getType(), r.getLocation(),
        r.getCapacity(), splitFacilities(r.getFacilities()), r.getPhoto(), r.isActive());
  }

  public Room toEntity(RoomRequest req) {
    return Room.builder()
        .name(req.name()
            .trim())
        .type(req.type())
        .location(req.location()
            .trim())
        .capacity(req.capacity())
        .facilities(joinFacilities(req.facilities()))
        .photo(req.photo())
        .active(req.active() == null || req.active())
        .build();
  }

  /**
   * Applies the request to an existing entity. The capacity must already be validated by the service.
   */
  public void updateEntity(Room r, RoomRequest req) {
    r.setName(req.name()
        .trim());
    r.setLocation(req.location()
        .trim());
    r.setFacilities(joinFacilities(req.facilities()));
    r.setPhoto(req.photo());
    r.setActive(req.active() == null || req.active());
    r.changeTypeAndCapacity(req.type(), req.capacity());
  }

  private List<String> splitFacilities(String facilities) {
    if (facilities == null || facilities.isBlank()) {
      return List.of();
    }
    return Arrays.stream(facilities.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  private String joinFacilities(List<String> facilities) {
    if (facilities == null || facilities.isEmpty()) {
      return null;
    }
    String joined = String.join(", ", facilities.stream()
        .map(String::trim)
        .toList());
    return joined.isBlank() ? null : joined;
  }
}


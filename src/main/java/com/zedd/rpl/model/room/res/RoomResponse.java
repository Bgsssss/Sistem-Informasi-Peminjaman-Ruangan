package com.zedd.rpl.model.room.res;

import com.zedd.rpl.enums.RoomType;
import java.util.List;

public record RoomResponse(
    Long id,
    String name,
    RoomType type,
    String location,
    Integer capacity,
    List<String> facilities,
    String photo,
    boolean active
) {}


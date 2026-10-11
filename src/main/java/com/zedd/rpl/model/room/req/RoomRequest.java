package com.zedd.rpl.model.room.req;

import com.zedd.rpl.enums.RoomType;
import jakarta.validation.constraints.*;

import java.util.List;

public record RoomRequest(
    @NotBlank @Size(max = 50) String name,
    @NotNull RoomType type,
    @NotBlank @Size(max = 150) String location,
    @NotNull @Min(2) @Max(8) Integer capacity,
    List<@NotBlank String> facilities,
    @Size(max = 255) String photo,
    Boolean active
) {}

package com.zedd.rpl.model.room.res;

public record RoomAdminResponse(RoomResponse room,
                                long affectedActiveBookings,
                                String manageBookingsLink) {
}

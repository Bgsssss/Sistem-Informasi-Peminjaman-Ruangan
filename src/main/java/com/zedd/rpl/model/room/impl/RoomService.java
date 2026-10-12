package com.zedd.rpl.model.room.impl;

import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.enums.RoomType;
import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.auditlog.AuditLog;
import com.zedd.rpl.model.auditlog.AuditLogRepository;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.room.Room;
import com.zedd.rpl.model.room.RoomMapper;
import com.zedd.rpl.model.room.RoomRepository;
import com.zedd.rpl.model.room.req.RoomRequest;
import com.zedd.rpl.model.room.res.RoomAdminResponse;
import com.zedd.rpl.model.room.res.RoomResponse;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;
  private final AuditLogRepository auditLogRepository;
  private final UserRepository userRepository;
  private final RoomMapper mapper;
  private final Clock clock;

  // ---------------- student side ----------------

  /** Active rooms only, with an optional type filter (FR-16). */
  @Transactional(readOnly = true)
  public List<RoomResponse> list(RoomType type) {
    List<Room> rooms = (type == null)
        ? roomRepository.findByActiveTrueOrderByName()
        : roomRepository.findByTypeAndActiveTrueOrderByName(type);
    return rooms.stream().map(mapper::toResponse).toList();
  }

  /** An inactive room can still be opened from an old link. active=false hides the booking button. */
  @Transactional(readOnly = true)
  public RoomResponse getDetail(Long id) {
    return mapper.toResponse(find(id));
  }

  // ---------------- admin side ----------------

  @Transactional(readOnly = true)
  public List<RoomResponse> listAll() {
    return roomRepository.findAllByOrderByName()
        .stream()
        .map(mapper::toResponse)
        .toList();
  }

  @Transactional
  public RoomAdminResponse create(RoomRequest req) {
    validate(req);
    if (roomRepository.existsByName(req.name().trim())) {
      throw new BusinessException(ErrorCode.ROOM_NAME_ALREADY_USED);
    }
    Room room = roomRepository.save(mapper.toEntity(req));
    return buildResponse(room);
  }

  @Transactional
  public RoomAdminResponse update(Long adminId, Long id, RoomRequest req) {
    validate(req);
    Room room = find(id);
    if (roomRepository.existsByNameAndIdNot(req.name().trim(), id)) {
      throw new BusinessException(ErrorCode.ROOM_NAME_ALREADY_USED);
    }
    boolean wasActive = room.isActive();
    mapper.updateEntity(room, req);

    if (wasActive && !room.isActive()) {
      User admin = userRepository.getReferenceById(adminId);
      auditLogRepository.save(new AuditLog(admin, "ROOM_DEACTIVATED", "room", room.getId(),
          "Room deactivated"));
    }
    return buildResponse(room);
  }

  /** A room with booking history cannot be deleted. Deactivate it instead. */
  @Transactional
  public void delete(Long adminId, Long id) {
    Room room = find(id);
    if (bookingRepository.existsByRoomId(id)) {
      throw new BusinessException(ErrorCode.ROOM_HAS_BOOKINGS);
    }
    User admin = userRepository.getReferenceById(adminId);
    auditLogRepository.save(new AuditLog(admin, "ROOM_DELETED", "room", room.getId(),
        "Room " + room.getName() + " deleted"));
    roomRepository.delete(room);
  }

  // ---------------- helpers ----------------

  private Room find(Long id) {
    return roomRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
  }

  private void validate(RoomRequest req) {
    if (!req.type().isCapacityValid(req.capacity())) {
      throw new BusinessException(ErrorCode.INVALID_CAPACITY, "Capacity for type " + req.type()
          + " must be " + req.type().getMinCapacity() + "-" + req.type().getMaxCapacity() + " people");
    }
  }

  /** When the room is inactive, include the affected active bookings and the Manage Bookings link (FR-33). */
  private RoomAdminResponse buildResponse(Room room) {
    long affected = 0;
    String link = null;
    if (!room.isActive()) {
      affected = bookingRepository.countByRoomIdAndStatusAndBookingDateGreaterThanEqual(
          room.getId(), BookingStatus.ACTIVE, LocalDate.now(clock));
      if (affected > 0) {
        link = "/api/admin/bookings?room=" + room.getId() + "&status=ACTIVE";
      }
    }
    return new RoomAdminResponse(mapper.toResponse(room), affected, link);
  }
}


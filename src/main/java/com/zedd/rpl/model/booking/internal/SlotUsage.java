package com.zedd.rpl.model.booking.internal;

import java.time.LocalTime;

public record SlotUsage(Long roomId, LocalTime startTime) {}


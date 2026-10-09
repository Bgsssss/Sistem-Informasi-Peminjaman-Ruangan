package com.zedd.rpl.enums;

import lombok.Getter;

@Getter
public enum RoomType {
  SMALL(2, 4),
  LARGE(4, 8);


  private final int minCapacity;
  private final int maxCapacity;

  RoomType(int minCapacity, int maxCapacity) {
    this.minCapacity = minCapacity;
    this.maxCapacity = maxCapacity;
  }

  public boolean isCapacityValid(int capacity) {
    return capacity >= minCapacity && capacity <= maxCapacity;
  }
}

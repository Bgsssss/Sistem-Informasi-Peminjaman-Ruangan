package com.zedd.rpl.model.room;

import com.zedd.rpl.common.BaseEntity;
import com.zedd.rpl.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Room extends BaseEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  // Tanpa setter terpisah: ubah lewat setTipeDanKapasitas() agar pasangannya selalu valid.
  @Setter(AccessLevel.NONE)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoomType type;

  @Column(nullable = false, length = 150)
  private String location;

  @Setter(AccessLevel.NONE)
  @JdbcTypeCode(SqlTypes.TINYINT)
  @Column(nullable = false)
  private Integer capacity;

  @Column
  private String facilities;

  @Column
  private String photo;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  public void changeTypeAndCapacity(RoomType type, int capacity) {
    this.type = type;
    this.capacity = capacity;
    validateCapacity();
  }

  /** Jaring pengaman untuk objek yang dibuat lewat builder. Database punya CHECK yang sama. */
  @PrePersist
  @PreUpdate
  void validateCapacity() {
    if (type == null || capacity == null || !type.isCapacityValid(capacity)) {
      throw new IllegalStateException(
          "Capacity " + capacity + " is outside the valid range for type " + type);
    }
  }
}

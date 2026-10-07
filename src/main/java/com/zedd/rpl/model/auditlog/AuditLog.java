package com.zedd.rpl.model.auditlog;

import com.zedd.rpl.model.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Tidak extend BaseEntity: log append-only, pelakunya sudah tercatat di kolom admin. */
@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "admin_id", nullable = false)
  private User admin;

  /** Contoh: BOOKING_CANCEL, RUANGAN_NONAKTIF. */
  @Column(nullable = false, length = 50)
  private String action;

  /** Contoh: booking, ruangan. */
  @Column(nullable = false, length = 50)
  private String entity;

  @Column(name = "entity_id", nullable = false)
  private Long entityId;

  @Column
  private String reason;

  @Column(name = "created_at", insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public AuditLog(User admin, String action, String entity, Long entityId, String reason) {
    this.admin = admin;
    this.action = action;
    this.entity = entity;
    this.entityId = entityId;
    this.reason = reason;
  }
}

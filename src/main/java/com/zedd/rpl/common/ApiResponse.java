package com.zedd.rpl.common;

import java.time.LocalDateTime;

/** Standard envelope for successful responses. Controllers wrap service results with it. */
public record ApiResponse<T>(boolean success, String message, T data, LocalDateTime timestamp) {

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
  }

  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, message, data, LocalDateTime.now());
  }

  public static ApiResponse<Void> message(String message) {
    return new ApiResponse<>(true, message, null, LocalDateTime.now());
  }
}

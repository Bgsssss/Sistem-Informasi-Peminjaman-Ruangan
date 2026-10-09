package com.zedd.rpl.model.student.res;

public record StudentResponse(
    Long id,
    String studentNumber,
    String name,
    String faculty,
    String email,
    boolean active,
    int trustScore
) {}


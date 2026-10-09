package com.zedd.rpl.model.trustcore;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrustScoreHistoryRepository extends JpaRepository<TrustScoreHistory, Long> {

  List<TrustScoreHistory> findByStudentIdOrderByCreatedAtDescIdDesc(Long studentId);

  void deleteByStudentId(Long studentId);
}


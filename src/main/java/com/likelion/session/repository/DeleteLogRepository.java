package com.likelion.session.repository;

import com.likelion.session.domain.DeleteLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeleteLogRepository extends JpaRepository<DeleteLog, Long> {
}
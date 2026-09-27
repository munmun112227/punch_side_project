package com.example.punch.repository;

import com.example.punch.entity.PunchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PunchRecordRepository extends JpaRepository<PunchRecord, Long> {
    List<PunchRecord> findAllByPunchTimeBetweenOrderByPunchTimeDesc(LocalDateTime start, LocalDateTime end);
}

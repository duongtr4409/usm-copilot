package com.usm.ams.repository;

import com.usm.ams.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<Outbox, UUID> {
}

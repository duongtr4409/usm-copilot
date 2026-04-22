package com.usm.ams.repository;

import com.usm.ams.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
	java.util.List<Enrollment> findByClassUnitId(UUID classUnitId);
}

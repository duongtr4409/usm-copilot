package com.usm.ams.repository;

import com.usm.ams.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
}

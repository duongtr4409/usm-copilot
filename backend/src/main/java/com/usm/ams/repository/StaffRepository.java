package com.usm.ams.repository;

import com.usm.ams.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StaffRepository extends JpaRepository<Staff, UUID> {
}

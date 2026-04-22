package com.usm.ams.repository;

import com.usm.ams.entity.OrganizationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, UUID> {
	List<OrganizationUnit> findByType(String type);
}

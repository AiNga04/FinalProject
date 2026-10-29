package org.vti.jamie.com.testingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.vti.jamie.com.testingsystem.entity.Department;

public interface IDepartmentRepository extends JpaRepository<Department, Long>,
        JpaSpecificationExecutor<Department> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}

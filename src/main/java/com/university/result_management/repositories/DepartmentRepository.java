package com.university.result_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.result_management.models.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {}

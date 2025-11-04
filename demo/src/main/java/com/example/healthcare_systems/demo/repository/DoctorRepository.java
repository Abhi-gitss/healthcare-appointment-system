package com.example.healthcare_systems.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.healthcare_systems.demo.Entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    List<Doctor> findByDepartment(String department);
    Doctor findByName(String name);
}

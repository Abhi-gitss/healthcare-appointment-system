package com.example.healthcare_systems.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.healthcare_systems.demo.Entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Integer> {}


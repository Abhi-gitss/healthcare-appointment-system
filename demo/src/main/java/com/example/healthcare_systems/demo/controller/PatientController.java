package com.example.healthcare_systems.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.healthcare_systems.demo.Entity.Patient;
import com.example.healthcare_systems.demo.repository.PatientRepository;

// PatientController.java
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    @Autowired
    private PatientRepository patientRepository;

    @PostMapping
    public Patient registerPatient(@RequestBody Patient patient) {
        return patientRepository.save(patient);
    }
}


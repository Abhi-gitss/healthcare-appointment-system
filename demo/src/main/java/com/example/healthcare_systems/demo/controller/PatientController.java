package com.example.healthcare_systems.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.healthcare_systems.demo.Entity.Patient;
import com.example.healthcare_systems.demo.repository.PatientRepository;
import com.example.healthcare_systems.demo.service.EmailService;

// PatientController.java
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    @Autowired
    private PatientRepository patientRepository;
    
    @Autowired
    private EmailService emailService;

    @PostMapping
    public Patient registerPatient(@RequestBody Patient patient) {
        Patient savedPatient = patientRepository.save(patient);
        
        // Send registration confirmation email
        if (savedPatient.getEmail() != null && !savedPatient.getEmail().isEmpty()) {
            emailService.sendRegistrationEmail(savedPatient);
        }
        
        return savedPatient;
    }
}


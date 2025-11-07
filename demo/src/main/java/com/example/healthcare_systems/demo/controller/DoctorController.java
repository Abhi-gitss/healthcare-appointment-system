package com.example.healthcare_systems.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.healthcare_systems.demo.Entity.Doctor;
import com.example.healthcare_systems.demo.repository.DoctorRepository;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "http://localhost:8080")
public class DoctorController {
    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping
    public List<Doctor> getDoctorsByDepartment(@RequestParam String department) {
        String normalized = department == null ? "" : department.trim();
        // Prefer case-insensitive match to avoid UI/DB mismatches
        List<Doctor> doctors = doctorRepository.findByDepartmentIgnoreCase(normalized);
        if (doctors == null || doctors.isEmpty()) {
            // Try contains on department
            doctors = doctorRepository.findByDepartmentContainingIgnoreCase(normalized);
        }
        if (doctors == null || doctors.isEmpty()) {
            // Try specialty exact and contains as fallback (some seeds may use specialty instead of department)
            doctors = doctorRepository.findBySpecialtyIgnoreCase(normalized);
        }
        if (doctors == null || doctors.isEmpty()) {
            doctors = doctorRepository.findBySpecialtyContainingIgnoreCase(normalized);
        }

        if (doctors == null || doctors.isEmpty()) {
            // Last fallback to exact department
            doctors = doctorRepository.findByDepartment(normalized);
        }
        return doctors;
    }
}

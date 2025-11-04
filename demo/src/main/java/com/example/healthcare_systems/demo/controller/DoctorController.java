package com.example.healthcare_systems.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.healthcare_systems.demo.Entity.Doctor;
import com.example.healthcare_systems.demo.repository.DoctorRepository;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping
    public List<Doctor> getDoctorsByDepartment(@RequestParam String department) {
        return doctorRepository.findByDepartment(department);
    }
}

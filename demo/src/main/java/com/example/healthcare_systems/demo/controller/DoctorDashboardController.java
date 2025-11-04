package com.example.healthcare_systems.demo.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.healthcare_systems.demo.Entity.Appointment;
import com.example.healthcare_systems.demo.Entity.Doctor;
import com.example.healthcare_systems.demo.service.AppointmentService;
import com.example.healthcare_systems.demo.repository.DoctorRepository;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class DoctorDashboardController {

    private final AppointmentService appointmentService;
    
    @Autowired
    private DoctorRepository doctorRepository;

    public DoctorDashboardController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/doctor-dashboard")
    public String doctorDashboard(Model model, Principal principal) {
        String doctorUsername = principal.getName();
        List<Appointment> appointments = appointmentService.findByDoctorUsername(doctorUsername);
        model.addAttribute("appointments", appointments);
        model.addAttribute("doctorName", doctorUsername); // Pass doctor name to template
        return "doctor-dashboard";
    }

    // --- API endpoint for frontend JS ---
    @ResponseBody
    @GetMapping("/api/doctors/appointments")
    public List<Appointment> getDoctorAppointments(Principal principal) {
        String doctorUsername = principal.getName();
        return appointmentService.findByDoctorUsername(doctorUsername);
    }
    
    // API endpoint to get logged-in doctor's info
    @ResponseBody
    @GetMapping("/api/doctors/me")
    public Map<String, Object> getCurrentDoctor(Principal principal) {
        String doctorUsername = principal.getName();
        Doctor doctor = doctorRepository.findByName(doctorUsername);
        
        Map<String, Object> response = new HashMap<>();
        if (doctor != null) {
            response.put("name", doctor.getName());
            response.put("doctorId", doctor.getDoctor_id());
            response.put("department", doctor.getDepartment());
        } else {
            response.put("name", doctorUsername); // Fallback to username
            response.put("doctorId", null);
            response.put("department", null);
        }
        return response;
    }
}

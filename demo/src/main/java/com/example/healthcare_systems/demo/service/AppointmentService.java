package com.example.healthcare_systems.demo.service;

import com.example.healthcare_systems.demo.Entity.Appointment;
import com.example.healthcare_systems.demo.repository.AppointmentRepository;
import com.example.healthcare_systems.demo.repository.DoctorRepository;
import com.example.healthcare_systems.demo.Entity.Doctor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Appointment> findByDoctorUsername(String doctorUsername) {
        // Get Doctor entity by username (Doctor.name matches the Spring Security username)
        Doctor doctor = doctorRepository.findByName(doctorUsername);
        if (doctor == null) {
            return List.of(); // No appointments if no doctor
        }
        String department = doctor.getDepartment();
        return appointmentRepository.findByDepartment(department);
    }

    // Cancel appointment (for patients and doctors)
    @Transactional
    public Appointment cancelAppointment(Integer appointmentId, Integer userId, String userRole) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        // Validate ownership: Patient can cancel their own, Doctor can cancel their own
        if ("PATIENT".equalsIgnoreCase(userRole)) {
            if (!appointment.getPatient_id().equals(userId)) {
                throw new RuntimeException("Patient can only cancel their own appointments");
            }
        } else if ("DOCTOR".equalsIgnoreCase(userRole)) {
            if (!appointment.getDoctor_id().equals(userId)) {
                throw new RuntimeException("Doctor can only cancel their own appointments");
            }
        }

        // Check if already cancelled
        if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Appointment is already cancelled");
        }

        // Check if completed - cannot cancel completed appointments
        if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Cannot cancel a completed appointment");
        }

        appointment.setStatus("Cancelled");
        return appointmentRepository.save(appointment);
    }

    // Reschedule appointment (update date/time)
    @Transactional
    public Appointment rescheduleAppointment(Integer appointmentId, LocalDate newDate, LocalTime newTime, Integer userId, String userRole) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        // Validate ownership
        if ("PATIENT".equalsIgnoreCase(userRole)) {
            if (!appointment.getPatient_id().equals(userId)) {
                throw new RuntimeException("Patient can only reschedule their own appointments");
            }
        } else if ("DOCTOR".equalsIgnoreCase(userRole)) {
            if (!appointment.getDoctor_id().equals(userId)) {
                throw new RuntimeException("Doctor can only reschedule their own appointments");
            }
        }

        // Check if already cancelled or completed
        if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Cannot reschedule a cancelled appointment");
        }
        if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
            throw new RuntimeException("Cannot reschedule a completed appointment");
        }

        // Validate new date/time is in the future
        LocalDate today = LocalDate.now();
        if (newDate.isBefore(today) || (newDate.equals(today) && newTime.isBefore(LocalTime.now()))) {
            throw new RuntimeException("Cannot reschedule to a past date/time");
        }

        // Validate working hours
        if (!isWithinWorkingSchedule(newDate.getDayOfWeek(), newTime)) {
            throw new RuntimeException("Selected time is outside working hours");
        }

        // Check daily limit for doctor (exclude current appointment from count)
        long booked = appointmentRepository.countForDoctorOnDate(appointment.getDoctor_id(), newDate);
        Doctor doctor = doctorRepository.findById(appointment.getDoctor_id())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        int maxAppointments = "General".equalsIgnoreCase(doctor.getDepartment()) ? 5 : 2;
        
        // If rescheduling to same date, allow if under limit
        // If rescheduling to different date, check if new date has space
        if (!newDate.equals(appointment.getAppointment_date())) {
            if (booked >= maxAppointments) {
                throw new RuntimeException("Doctor already has " + maxAppointments + " appointments for this day");
            }
        }

        appointment.setAppointment_date(newDate);
        appointment.setAppointment_time(newTime);
        return appointmentRepository.save(appointment);
    }

    // Update appointment status
    @Transactional
    public Appointment updateAppointmentStatus(Integer appointmentId, String newStatus, Integer userId, String userRole) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        // Validate status transition
        String currentStatus = appointment.getStatus();
        if ("Cancelled".equalsIgnoreCase(currentStatus)) {
            throw new RuntimeException("Cannot change status of a cancelled appointment");
        }
        if ("Completed".equalsIgnoreCase(currentStatus) && !"Completed".equalsIgnoreCase(newStatus)) {
            throw new RuntimeException("Cannot change status of a completed appointment");
        }

        // Validate valid status values
        if (!isValidStatus(newStatus)) {
            throw new RuntimeException("Invalid status: " + newStatus);
        }

        // Doctors can update to any status, patients can only cancel
        if ("PATIENT".equalsIgnoreCase(userRole) && !"Cancelled".equalsIgnoreCase(newStatus)) {
            if (!appointment.getPatient_id().equals(userId)) {
                throw new RuntimeException("Patient can only cancel their own appointments");
            }
            if (!"Cancelled".equalsIgnoreCase(newStatus)) {
                throw new RuntimeException("Patients can only cancel appointments, not change to other statuses");
            }
        }

        appointment.setStatus(newStatus);
        return appointmentRepository.save(appointment);
    }

    // Filter appointments with multiple criteria
    public List<Appointment> findWithFilters(Integer doctorId, Integer patientId, String status, 
                                              LocalDate startDate, LocalDate endDate) {
        return appointmentRepository.findWithFilters(doctorId, patientId, status, startDate, endDate);
    }

    // Helper method to check working schedule
    private boolean isWithinWorkingSchedule(DayOfWeek day, LocalTime time) {
        LocalTime start;
        LocalTime end = LocalTime.of(17, 0);
        switch (day) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
                start = LocalTime.of(8, 0);
                break;
            case THURSDAY:
            case FRIDAY:
                start = LocalTime.of(9, 0);
                break;
            case SATURDAY:
            case SUNDAY:
                start = LocalTime.of(10, 0);
                break;
            default:
                start = LocalTime.of(8, 0);
        }
        return !time.isBefore(start) && !time.isAfter(end);
    }

    // Helper method to validate status
    private boolean isValidStatus(String status) {
        return status != null && (
            "Scheduled".equalsIgnoreCase(status) ||
            "Completed".equalsIgnoreCase(status) ||
            "Cancelled".equalsIgnoreCase(status) ||
            "Pending".equalsIgnoreCase(status) ||
            "In Progress".equalsIgnoreCase(status)
        );
    }

    // Get appointment by ID with authorization check
    public Appointment getAppointmentById(Integer appointmentId, Integer userId, String userRole) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        // Check authorization
        if ("PATIENT".equalsIgnoreCase(userRole)) {
            if (!appointment.getPatient_id().equals(userId)) {
                throw new RuntimeException("Access denied: Patient can only view their own appointments");
            }
        } else if ("DOCTOR".equalsIgnoreCase(userRole)) {
            if (!appointment.getDoctor_id().equals(userId)) {
                throw new RuntimeException("Access denied: Doctor can only view their own appointments");
            }
        }

        return appointment;
    }
}
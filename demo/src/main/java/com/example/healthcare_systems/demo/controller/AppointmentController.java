package com.example.healthcare_systems.demo.controller;
import com.example.healthcare_systems.demo.Entity.Appointment;
import com.example.healthcare_systems.demo.repository.AppointmentRepository;
import com.example.healthcare_systems.demo.repository.PatientRepository;
import com.example.healthcare_systems.demo.service.AppointmentService;


import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.DayOfWeek;
import java.security.Principal;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private AppointmentService appointmentService;

    // Create new appointment (booking)
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody Map<String, Object> payload) {
        try {
            Integer patientId = Integer.valueOf(String.valueOf(payload.get("patient_id")));
            Integer doctorId = Integer.valueOf(String.valueOf(payload.get("doctor_id")));
            String department = String.valueOf(payload.get("department"));
            String dateStr = String.valueOf(payload.get("appointment_date"));
            String timeStr = String.valueOf(payload.get("appointment_time"));
            String status = String.valueOf(payload.getOrDefault("status", "Scheduled"));
            String reason = String.valueOf(payload.getOrDefault("reason", ""));

            if (!patientRepository.existsById(patientId)) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(Map.of("error", "PATIENT_NOT_FOUND"));
            }

            LocalDate apptDate = LocalDate.parse(dateStr);
            LocalTime apptTime = LocalTime.parse(timeStr);

            // Enforce working schedule windows
            if (!isWithinWorkingSchedule(apptDate.getDayOfWeek(), apptTime)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "OUT_OF_SCHEDULE",
                                "message", "Selected time is outside working hours"
                        ));
            }

            // Enforce per-doctor per-day limit: max 2
            long booked = appointmentRepository.countForDoctorOnDate(doctorId, apptDate);
            if (booked >= 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "DAILY_LIMIT_REACHED",
                                "message", "Doctor already has 2 appointments for this day"
                        ));
            }

            Appointment appt = new Appointment();
            appt.setPatient_id(patientId);
            appt.setDoctor_id(doctorId);
            appt.setDepartment(department);
            appt.setAppointment_date(apptDate);
            appt.setAppointment_time(apptTime);
            appt.setStatus(status);
            appt.setReason(reason);

            return ResponseEntity.ok(appointmentRepository.save(appt));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "INVALID_PAYLOAD", "message", ex.getMessage()));
        }
    }

    private boolean isWithinWorkingSchedule(DayOfWeek day, LocalTime time) {
        // Working hours mapping based on site display:
        // Mon-Wed: 08:00 - 17:00
        // Thu-Fri: 09:00 - 17:00
        // Sat-Sun: 10:00 - 17:00
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

    // Get all appointments
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // Get appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String userRole) {
        try {
            Appointment appointment;
            if (userId != null && userRole != null) {
                appointment = appointmentService.getAppointmentById(id, userId, userRole);
            } else {
                appointment = appointmentRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));
            }
            return ResponseEntity.ok(appointment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "message", e.getMessage()));
        }
    }

    // Cancel appointment
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String userRole,
            Principal principal) {
        try {
            // If userId/role not provided, try to get from Principal (for doctors)
            if (userId == null && principal != null) {
                // For now, we'll need userId from request - in future, link Principal to Doctor ID
                // For this implementation, userId must be provided
                if (userRole == null) {
                    userRole = "DOCTOR"; // Assume doctor if using Principal
                }
            }
            
            if (userId == null || userRole == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "MISSING_PARAMS", 
                                "message", "userId and userRole are required"));
            }

            Appointment cancelled = appointmentService.cancelAppointment(id, userId, userRole);
            return ResponseEntity.ok(Map.of(
                    "message", "Appointment cancelled successfully",
                    "appointment", cancelled
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "CANCELLATION_FAILED", "message", e.getMessage()));
        }
    }

    // Reschedule appointment
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<?> rescheduleAppointment(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String userRole,
            Principal principal) {
        try {
            if (userId == null || userRole == null) {
                if (principal != null && userRole == null) {
                    userRole = "DOCTOR";
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "MISSING_PARAMS", 
                                    "message", "userId and userRole are required"));
                }
            }

            LocalDate newDate = LocalDate.parse(String.valueOf(payload.get("appointment_date")));
            LocalTime newTime = LocalTime.parse(String.valueOf(payload.get("appointment_time")));

            Appointment rescheduled = appointmentService.rescheduleAppointment(id, newDate, newTime, userId, userRole);
            return ResponseEntity.ok(Map.of(
                    "message", "Appointment rescheduled successfully",
                    "appointment", rescheduled
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "RESCHEDULE_FAILED", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "INVALID_PAYLOAD", "message", e.getMessage()));
        }
    }

    // Update appointment status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String userRole,
            Principal principal) {
        try {
            if (userId == null || userRole == null) {
                if (principal != null && userRole == null) {
                    userRole = "DOCTOR";
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "MISSING_PARAMS", 
                                    "message", "userId and userRole are required"));
                }
            }

            String newStatus = String.valueOf(payload.get("status"));
            if (newStatus == null || "null".equals(newStatus)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "MISSING_STATUS", 
                                "message", "status field is required"));
            }

            Appointment updated = appointmentService.updateAppointmentStatus(id, newStatus, userId, userRole);
            return ResponseEntity.ok(Map.of(
                    "message", "Appointment status updated successfully",
                    "appointment", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "STATUS_UPDATE_FAILED", "message", e.getMessage()));
        }
    }

    // Filter appointments with multiple criteria
    @GetMapping("/search")
    public ResponseEntity<?> filterAppointments(
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            List<Appointment> appointments = appointmentService.findWithFilters(
                    doctorId, patientId, status, start, end);

            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "FILTER_FAILED", "message", e.getMessage()));
        }
    }

}

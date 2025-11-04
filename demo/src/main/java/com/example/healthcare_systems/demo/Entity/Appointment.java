package com.example.healthcare_systems.demo.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Timestamp;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appointment_id;

    private Integer patient_id;
    @Column(name = "doctor_id")
    private Integer doctorId;
    private String department;
    private LocalDate appointment_date;
    private LocalTime appointment_time;
    private String status; // Enum as String (Scheduled, Completed, Cancelled, Pending)
    private String reason;
    private Timestamp created_at;
    public Integer getPatient_id() {
        return patient_id;
    }
    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }
    public Integer getDoctor_id() {
        return doctorId;
    }
    public void setDoctor_id(Integer doctor_id) {
        this.doctorId = doctor_id;
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public LocalDate getAppointment_date() {
        return appointment_date;
    }
    public void setAppointment_date(LocalDate appointment_date) {
        this.appointment_date = appointment_date;
    }
    public LocalTime getAppointment_time() {
        return appointment_time;
    }
    public void setAppointment_time(LocalTime appointment_time) {
        this.appointment_time = appointment_time;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
    public Timestamp getCreated_at() {
        return created_at;
    }
    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Integer getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Integer appointment_id) {
        this.appointment_id = appointment_id;
    }


}

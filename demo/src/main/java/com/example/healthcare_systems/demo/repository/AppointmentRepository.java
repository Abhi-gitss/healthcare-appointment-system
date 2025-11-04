package com.example.healthcare_systems.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

import com.example.healthcare_systems.demo.Entity.Appointment;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    // Basic queries
    List<Appointment> findByDoctorId(Integer doctorId);
    List<Appointment> findByDepartment(String department);
    
    // Using @Query for fields with underscores (patient_id, appointment_date)
    @Query("SELECT a FROM Appointment a WHERE a.patient_id = :patientId")
    List<Appointment> findByPatientId(@Param("patientId") Integer patientId);
    
    // Status-based queries
    List<Appointment> findByStatus(String status);
    List<Appointment> findByDoctorIdAndStatus(Integer doctorId, String status);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient_id = :patientId AND a.status = :status")
    List<Appointment> findByPatientIdAndStatus(@Param("patientId") Integer patientId, @Param("status") String status);
    
    // Date range queries
    @Query("SELECT a FROM Appointment a WHERE a.appointment_date BETWEEN :startDate AND :endDate")
    List<Appointment> findByAppointmentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointment_date BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(@Param("doctorId") Integer doctorId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Appointment a WHERE a.patient_id = :patientId AND a.appointment_date BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndAppointmentDateBetween(@Param("patientId") Integer patientId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    // Combined filters
    @Query("SELECT a FROM Appointment a WHERE " +
           "(:doctorId IS NULL OR a.doctorId = :doctorId) AND " +
           "(:patientId IS NULL OR a.patient_id = :patientId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:startDate IS NULL OR a.appointment_date >= :startDate) AND " +
           "(:endDate IS NULL OR a.appointment_date <= :endDate)")
    List<Appointment> findWithFilters(
        @Param("doctorId") Integer doctorId,
        @Param("patientId") Integer patientId,
        @Param("status") String status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Count query for daily limit (existing)
    @Query("select count(a) from Appointment a where a.doctorId = :doctorId and a.appointment_date = :date and a.status != 'Cancelled'")
    long countForDoctorOnDate(@Param("doctorId") Integer doctorId, @Param("date") LocalDate date);
}

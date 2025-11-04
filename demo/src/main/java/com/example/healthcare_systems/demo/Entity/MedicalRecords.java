package com.example.healthcare_systems.demo.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.sql.Timestamp;

@Entity
@Table(name = "medical_records")
public class MedicalRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer record_id;

    private Integer patient_id;
    private Integer doctor_id;
    private String diagnosis;
    private String prescription;
    private String test_results;
    private String notes;
    private LocalDate record_date;
    private Timestamp created_at;
    public Integer getPatient_id() {
        return patient_id;
    }
    public void setPatient_id(Integer patient_id) {
        this.patient_id = patient_id;
    }
    public Integer getDoctor_id() {
        return doctor_id;
    }
    public void setDoctor_id(Integer doctor_id) {
        this.doctor_id = doctor_id;
    }
    public String getDiagnosis() {
        return diagnosis;
    }
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    public String getPrescription() {
        return prescription;
    }
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
    public String getTest_results() {
        return test_results;
    }
    public void setTest_results(String test_results) {
        this.test_results = test_results;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public LocalDate getRecord_date() {
        return record_date;
    }
    public void setRecord_date(LocalDate record_date) {
        this.record_date = record_date;
    }
    public Timestamp getCreated_at() {
        return created_at;
    }
    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    // Getters and Setters
}

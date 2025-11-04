package com.example.healthcare_systems.demo.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Departments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer department_id;

    private String department_name;
    private String description;
    private Integer head_doctor_id;
    public String getDepartment_name() {
        return department_name;
    }
    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Integer getHead_doctor_id() {
        return head_doctor_id;
    }
    public void setHead_doctor_id(Integer head_doctor_id) {
        this.head_doctor_id = head_doctor_id;
    }

    // Getters and Setters
}

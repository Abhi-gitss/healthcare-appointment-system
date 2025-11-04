package com.example.healthcare_systems.demo.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer patient_id;

    private String name;

    private String email;

    private String phone;

    private LocalDate date_of_birth;

    @Enumerated(EnumType.STRING)
    private Gender gender; // Enum for "Male"/"Female"/"Other"

    private String address;

    private String blood_group;

    // Enum definition for Gender
    public enum Gender {
        Male, Female, Other
    }

    // Getters and setters (required for JPA)
    public Integer getPatient_id() { return patient_id; }
    public void setPatient_id(Integer patient_id) { this.patient_id = patient_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDate_of_birth() { return date_of_birth; }
    public void setDate_of_birth(LocalDate date_of_birth) { this.date_of_birth = date_of_birth; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBlood_group() { return blood_group; }
    public void setBlood_group(String blood_group) { this.blood_group = blood_group; }
}

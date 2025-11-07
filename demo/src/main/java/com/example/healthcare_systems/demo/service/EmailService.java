package com.example.healthcare_systems.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.example.healthcare_systems.demo.Entity.Patient;
import com.example.healthcare_systems.demo.Entity.Doctor;
import com.example.healthcare_systems.demo.Entity.Appointment;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final String FROM_EMAIL = "bkrafton03@gmail.com"; // Change this to your email
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    /**
     * Send registration confirmation email to patient
     */
    public void sendRegistrationEmail(Patient patient) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(patient.getEmail());
            message.setSubject("Welcome to Healthcare System - Registration Successful");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Thank you for registering with our Healthcare System!\n\n" +
                "Your registration details:\n" +
                "Patient ID: %d\n" +
                "Name: %s\n" +
                "Email: %s\n" +
                "Phone: %s\n" +
                "Date of Birth: %s\n" +
                "Gender: %s\n" +
                "Blood Group: %s\n\n" +
                "You can now book appointments using your Patient ID.\n\n" +
                "Best regards,\n" +
                "Healthcare System Team",
                patient.getName(),
                patient.getPatient_id(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getDate_of_birth() != null ? patient.getDate_of_birth().format(DATE_FORMATTER) : "N/A",
                patient.getGender() != null ? patient.getGender().toString() : "N/A",
                patient.getBlood_group() != null ? patient.getBlood_group() : "N/A"
            );
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw - email failure shouldn't break registration
            System.err.println("Failed to send registration email: " + e.getMessage());
        }
    }

    /**
     * Send appointment confirmation email when appointment is created
     */
    public void sendAppointmentConfirmationEmail(Appointment appointment, Patient patient, Doctor doctor) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(patient.getEmail());
            message.setSubject("Appointment Confirmation - Healthcare System");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been successfully booked!\n\n" +
                "Appointment Details:\n" +
                "Appointment ID: %d\n" +
                "Doctor: %s\n" +
                "Department: %s\n" +
                "Date: %s\n" +
                "Time: %s\n" +
                "Reason: %s\n" +
                "Status: %s\n\n" +
                "Please arrive 10 minutes before your scheduled time.\n\n" +
                "If you need to reschedule or cancel, please contact us or use the patient portal.\n\n" +
                "Best regards,\n" +
                "Healthcare System Team",
                patient.getName(),
                appointment.getAppointment_id(),
                doctor != null ? doctor.getName() : "N/A",
                appointment.getDepartment(),
                appointment.getAppointment_date() != null ? appointment.getAppointment_date().format(DATE_FORMATTER) : "N/A",
                appointment.getAppointment_time() != null ? appointment.getAppointment_time().format(TIME_FORMATTER) : "N/A",
                appointment.getReason() != null && !appointment.getReason().isEmpty() ? appointment.getReason() : "General Consultation",
                appointment.getStatus()
            );
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send appointment confirmation email: " + e.getMessage());
        }
    }

    /**
     * Send email when appointment status is updated by doctor
     */
    public void sendAppointmentStatusUpdateEmail(Appointment appointment, Patient patient, Doctor doctor, String oldStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(patient.getEmail());
            message.setSubject("Appointment Status Update - Healthcare System");
            
            String statusMessage = "";
            switch (appointment.getStatus().toLowerCase()) {
                case "completed":
                    statusMessage = "Your appointment has been marked as Completed.";
                    break;
                case "cancelled":
                    statusMessage = "Your appointment has been Cancelled.";
                    break;
                case "in progress":
                    statusMessage = "Your appointment is now In Progress.";
                    break;
                case "pending":
                    statusMessage = "Your appointment status has been updated to Pending.";
                    break;
                default:
                    statusMessage = "Your appointment status has been updated.";
            }
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "%s\n\n" +
                "Appointment Details:\n" +
                "Appointment ID: %d\n" +
                "Doctor: %s\n" +
                "Department: %s\n" +
                "Date: %s\n" +
                "Time: %s\n" +
                "Previous Status: %s\n" +
                "Current Status: %s\n\n" +
                "If you have any questions, please contact us.\n\n" +
                "Best regards,\n" +
                "Healthcare System Team",
                patient.getName(),
                statusMessage,
                appointment.getAppointment_id(),
                doctor != null ? doctor.getName() : "N/A",
                appointment.getDepartment(),
                appointment.getAppointment_date() != null ? appointment.getAppointment_date().format(DATE_FORMATTER) : "N/A",
                appointment.getAppointment_time() != null ? appointment.getAppointment_time().format(TIME_FORMATTER) : "N/A",
                oldStatus != null ? oldStatus : "N/A",
                appointment.getStatus()
            );
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send status update email: " + e.getMessage());
        }
    }

    /**
     * Send email when appointment is rescheduled
     */
    public void sendAppointmentRescheduleEmail(Appointment appointment, Patient patient, Doctor doctor, 
                                               String oldDate, String oldTime) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(patient.getEmail());
            message.setSubject("Appointment Rescheduled - Healthcare System");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been rescheduled.\n\n" +
                "Appointment Details:\n" +
                "Appointment ID: %d\n" +
                "Doctor: %s\n" +
                "Department: %s\n\n" +
                "Previous Schedule:\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "New Schedule:\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "Please make a note of the new date and time.\n\n" +
                "If you have any questions or concerns, please contact us.\n\n" +
                "Best regards,\n" +
                "Healthcare System Team",
                patient.getName(),
                appointment.getAppointment_id(),
                doctor != null ? doctor.getName() : "N/A",
                appointment.getDepartment(),
                oldDate != null ? oldDate : "N/A",
                oldTime != null ? oldTime : "N/A",
                appointment.getAppointment_date() != null ? appointment.getAppointment_date().format(DATE_FORMATTER) : "N/A",
                appointment.getAppointment_time() != null ? appointment.getAppointment_time().format(TIME_FORMATTER) : "N/A"
            );
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send reschedule email: " + e.getMessage());
        }
    }

    /**
     * Send email when appointment is cancelled
     */
    public void sendAppointmentCancellationEmail(Appointment appointment, Patient patient, Doctor doctor) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(FROM_EMAIL);
            message.setTo(patient.getEmail());
            message.setSubject("Appointment Cancelled - Healthcare System");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been cancelled.\n\n" +
                "Cancelled Appointment Details:\n" +
                "Appointment ID: %d\n" +
                "Doctor: %s\n" +
                "Department: %s\n" +
                "Date: %s\n" +
                "Time: %s\n" +
                "Reason: %s\n\n" +
                "If you would like to book a new appointment, please visit our website or contact us.\n\n" +
                "Best regards,\n" +
                "Healthcare System Team",
                patient.getName(),
                appointment.getAppointment_id(),
                doctor != null ? doctor.getName() : "N/A",
                appointment.getDepartment(),
                appointment.getAppointment_date() != null ? appointment.getAppointment_date().format(DATE_FORMATTER) : "N/A",
                appointment.getAppointment_time() != null ? appointment.getAppointment_time().format(TIME_FORMATTER) : "N/A",
                appointment.getReason() != null && !appointment.getReason().isEmpty() ? appointment.getReason() : "N/A"
            );
            
            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }
}


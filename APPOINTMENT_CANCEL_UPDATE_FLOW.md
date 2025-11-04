# Appointment Cancel & Update - Complete Implementation Flow

## Overview
This document explains how appointment cancellation and status updates work, from the frontend button click all the way to the database update.

---

## 🔄 Complete Flow Diagram

```
Frontend (Browser) → Controller → Service → Repository → Database
   ↓                ↓            ↓          ↓            ↓
  Button Click   REST API    Business    JPA         SQL UPDATE
                 Endpoint    Logic       ORM
```

---

## 📍 Step-by-Step Flow

### **STEP 1: Frontend - User Clicks Button**

**File:** `doctor-dashboard.html` (lines 132-153)

```javascript
function cancelAppointment(appointmentId) {
    // 1. User confirmation
    if (!confirm('Are you sure you want to cancel this appointment?')) {
        return;
    }
    
    // 2. Make HTTP PUT request to backend
    fetch(`/api/appointments/${appointmentId}/cancel?userId=${currentDoctorId}&userRole=DOCTOR`, {
        method: 'PUT'  // HTTP method for update operations
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert('Error: ' + data.message);
        } else {
            alert('Appointment cancelled successfully!');
            location.reload(); // Refresh page to show updated data
        }
    })
}
```

**What happens:**
- User clicks "Cancel" button on an appointment
- JavaScript sends HTTP PUT request to `/api/appointments/{id}/cancel`
- Includes `userId` and `userRole` as query parameters
- Waits for response and shows success/error message

---

### **STEP 2: Controller - Receives HTTP Request**

**File:** `AppointmentController.java` (lines 139-171)

```java
@PutMapping("/{id}/cancel")
public ResponseEntity<?> cancelAppointment(
        @PathVariable Integer id,                    // Appointment ID from URL
        @RequestParam(required = false) Integer userId,  // Doctor/Patient ID
        @RequestParam(required = false) String userRole, // "DOCTOR" or "PATIENT"
        Principal principal) {                        // Spring Security user info
    try {
        // Validate parameters
        if (userId == null || userRole == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "MISSING_PARAMS", 
                            "message", "userId and userRole are required"));
        }

        // Call service layer to handle business logic
        Appointment cancelled = appointmentService.cancelAppointment(id, userId, userRole);
        
        // Return success response
        return ResponseEntity.ok(Map.of(
                "message", "Appointment cancelled successfully",
                "appointment", cancelled
        ));
    } catch (RuntimeException e) {
        // Return error response
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "CANCELLATION_FAILED", "message", e.getMessage()));
    }
}
```

**What happens:**
- Spring MVC receives the HTTP PUT request
- Extracts appointment ID from URL path (`/{id}`)
- Extracts `userId` and `userRole` from query parameters
- Validates that required parameters are present
- Calls the service layer method
- Returns JSON response (success or error)

---

### **STEP 3: Service Layer - Business Logic & Validation**

**File:** `AppointmentService.java` (lines 37-65)

```java
@Transactional  // ⚠️ IMPORTANT: Ensures database transaction
public Appointment cancelAppointment(Integer appointmentId, Integer userId, String userRole) {
    // 1. Fetch appointment from database
    Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

    // 2. VALIDATION: Check ownership
    if ("PATIENT".equalsIgnoreCase(userRole)) {
        if (!appointment.getPatient_id().equals(userId)) {
            throw new RuntimeException("Patient can only cancel their own appointments");
        }
    } else if ("DOCTOR".equalsIgnoreCase(userRole)) {
        if (!appointment.getDoctor_id().equals(userId)) {
            throw new RuntimeException("Doctor can only cancel their own appointments");
        }
    }

    // 3. VALIDATION: Check if already cancelled
    if ("Cancelled".equalsIgnoreCase(appointment.getStatus())) {
        throw new RuntimeException("Appointment is already cancelled");
    }

    // 4. VALIDATION: Cannot cancel completed appointments
    if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
        throw new RuntimeException("Cannot cancel a completed appointment");
    }

    // 5. UPDATE: Change status to "Cancelled"
    appointment.setStatus("Cancelled");
    
    // 6. SAVE: Persist changes to database
    return appointmentRepository.save(appointment);
}
```

**What happens:**
1. **Fetches appointment** from database using `findById()`
2. **Validates ownership** - ensures user can only cancel their own appointments
3. **Validates business rules** - prevents cancelling already cancelled or completed appointments
4. **Modifies the object** - sets status to "Cancelled"
5. **Saves to database** - calls `repository.save()` which triggers SQL UPDATE

**Key Points:**
- `@Transactional` annotation ensures:
  - All database operations happen atomically (all-or-nothing)
  - If any error occurs, all changes are rolled back
  - Ensures data consistency

---

### **STEP 4: Repository Layer - Database Update**

**File:** `AppointmentRepository.java` (extends `JpaRepository`)

```java
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    // No custom save method needed - inherited from JpaRepository
    // JpaRepository provides: save(), findById(), findAll(), delete(), etc.
}
```

**What happens when `save()` is called:**
- Spring Data JPA checks if the appointment exists (has an ID)
- Since it exists, it performs an **UPDATE** operation
- Generates SQL: `UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = ?`
- Executes the SQL against the database
- Returns the updated entity

**Under the hood (Hibernate/JPA):**
```sql
-- Hibernate automatically generates and executes:
UPDATE appointments 
SET status = 'Cancelled' 
WHERE appointment_id = 5;
```

---

### **STEP 5: Database - Physical Update**

**MySQL Database Table: `appointments`**

**Before:**
```
| appointment_id | patient_id | doctor_id | status    | appointment_date | ... |
|----------------|------------|-----------|-----------|------------------|-----|
| 5              | 2          | 1         | Scheduled | 2024-12-25       | ... |
```

**After:**
```
| appointment_id | patient_id | doctor_id | status    | appointment_date | ... |
|----------------|------------|-----------|-----------|------------------|-----|
| 5              | 2          | 1         | Cancelled | 2024-12-25       | ... |
```

**What happens:**
- MySQL receives the UPDATE SQL statement
- Finds the row with `appointment_id = 5`
- Updates the `status` column from "Scheduled" to "Cancelled"
- Commits the transaction (because of `@Transactional`)

---

## 🔄 Complete Update Status Flow

### **Frontend Call** (doctor-dashboard.html, lines 156-179)

```javascript
function updateStatus(appointmentId) {
    const newStatus = prompt('Enter new status:');
    fetch(`/api/appointments/${appointmentId}/status?userId=${currentDoctorId}&userRole=DOCTOR`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({ status: newStatus })  // Request body with new status
    })
}
```

### **Controller** (AppointmentController.java, lines 209-244)

```java
@PutMapping("/{id}/status")
public ResponseEntity<?> updateAppointmentStatus(
        @PathVariable Integer id,
        @RequestBody Map<String, Object> payload,  // Receives JSON body
        @RequestParam Integer userId,
        @RequestParam String userRole) {
    
    String newStatus = String.valueOf(payload.get("status"));
    Appointment updated = appointmentService.updateAppointmentStatus(id, newStatus, userId, userRole);
    return ResponseEntity.ok(Map.of("message", "Status updated", "appointment", updated));
}
```

### **Service** (AppointmentService.java, lines 122-154)

```java
@Transactional
public Appointment updateAppointmentStatus(Integer appointmentId, String newStatus, ...) {
    // 1. Fetch appointment
    Appointment appointment = appointmentRepository.findById(appointmentId)...;
    
    // 2. Validate status transitions
    if ("Cancelled".equalsIgnoreCase(currentStatus)) {
        throw new RuntimeException("Cannot change status of a cancelled appointment");
    }
    
    // 3. Validate new status value
    if (!isValidStatus(newStatus)) {
        throw new RuntimeException("Invalid status: " + newStatus);
    }
    
    // 4. Update status
    appointment.setStatus(newStatus);
    
    // 5. Save to database
    return appointmentRepository.save(appointment);
}
```

### **Database Update**
```sql
UPDATE appointments 
SET status = 'Completed' 
WHERE appointment_id = 5;
```

---

## 🔑 Key Concepts Explained

### 1. **@Transactional Annotation**
```java
@Transactional
public Appointment cancelAppointment(...) {
    // All database operations in this method are wrapped in a transaction
}
```

**Benefits:**
- **Atomicity**: All operations succeed or all fail (no partial updates)
- **Consistency**: Database stays in valid state
- **Isolation**: Other transactions don't see partial changes
- **Durability**: Once committed, changes are permanent

**Example:** If validation fails after updating status, the entire operation rolls back.

### 2. **JPA Entity State Management**
```java
Appointment appointment = repository.findById(5);  // Entity is "managed"
appointment.setStatus("Cancelled");                // Modifies entity in memory
repository.save(appointment);                       // Syncs changes to database
```

**JPA automatically:**
- Tracks changes to managed entities
- Generates SQL UPDATE statements
- Maps Java objects to database rows

### 3. **Entity Relationship**
```java
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    private Integer appointment_id;
    private String status;  // This field maps to `status` column in database
    // ...
}
```

**Mapping:**
- Java field `status` → Database column `status`
- Java object `Appointment` → Database row in `appointments` table

---

## 📊 Data Flow Summary

| Layer | Responsibility | Input | Output |
|-------|---------------|-------|--------|
| **Frontend** | User interaction | Button click | HTTP PUT request |
| **Controller** | Request handling | HTTP request | Service method call |
| **Service** | Business logic & validation | Appointment ID, User info | Updated Appointment object |
| **Repository** | Database operations | Appointment object | SQL UPDATE execution |
| **Database** | Data storage | SQL UPDATE | Updated row |

---

## 🛡️ Security & Validation Layers

1. **Frontend Validation**: User confirmation prompt
2. **Controller Validation**: Parameter existence check
3. **Service Validation**: 
   - Ownership verification
   - Business rule enforcement
   - Status transition validation
4. **Database Constraints**: Primary keys, foreign keys (if configured)

---

## 🔍 How to Verify Database Update

### Method 1: Check in Application
- Reload dashboard after cancellation
- Status should show "Cancelled" badge

### Method 2: Check Database Directly
```sql
SELECT appointment_id, status, appointment_date, appointment_time 
FROM appointments 
WHERE appointment_id = 5;
```

### Method 3: Check Application Logs
Look for Hibernate SQL logs:
```
Hibernate: update appointments set status=? where appointment_id=?
```

---

## 🎯 Important Points

1. **Direct Database Update**: Yes, the update happens directly through JPA/Hibernate
2. **Transaction Safety**: `@Transactional` ensures data integrity
3. **Validation**: Multiple layers of validation prevent invalid updates
4. **Authorization**: Ownership checks ensure users can only modify their own appointments
5. **Automatic SQL Generation**: Spring Data JPA generates SQL automatically - no manual SQL needed

---

## 💡 Why This Architecture?

- **Separation of Concerns**: Each layer has a single responsibility
- **Maintainability**: Easy to modify business logic without touching database code
- **Testability**: Can test each layer independently
- **Type Safety**: Java objects instead of raw SQL strings
- **Transaction Management**: Automatic rollback on errors

---

## 🚀 Testing the Flow

1. **Start application**: Run `DemoApplication`
2. **Login**: Use doctor credentials
3. **View dashboard**: See appointments with Cancel/Update buttons
4. **Click Cancel**: Watch browser network tab to see HTTP request
5. **Check logs**: See Hibernate SQL in console
6. **Verify**: Check database or reload dashboard

---

This is how the complete flow works from button click to database update! 🎉


# How Patients Can Cancel Their Appointments

## Overview
Patients can view and cancel their appointments through a **Patient Portal** interface. This guide explains how it works.

---

## 🎯 Patient Portal Features

### **Access the Portal:**
1. Go to: `http://localhost:8080/patient-portal.html`
2. Or click the link on the appointment booking page: "View & Manage Your Appointments"

### **What Patients Can Do:**
- ✅ View all their appointments
- ✅ See appointment details (date, time, department, doctor, status)
- ✅ Cancel scheduled or pending appointments
- ✅ View appointment status (Scheduled, Completed, Cancelled, Pending)

### **What Patients Cannot Do:**
- ❌ Cannot cancel completed appointments
- ❌ Cannot cancel already cancelled appointments
- ❌ Cannot change status to anything other than "Cancelled"
- ❌ Cannot manage other patients' appointments

---

## 📋 Step-by-Step Process

### **Step 1: Access Patient Portal**
```
URL: http://localhost:8080/patient-portal.html
```

### **Step 2: Enter Patient ID**
- Enter your Patient ID (provided when you registered)
- Click "View My Appointments"

### **Step 3: View Appointments**
- All your appointments are displayed in cards
- Each card shows:
  - Appointment ID
  - Date and Time
  - Department
  - Doctor ID
  - Reason (if provided)
  - Current Status

### **Step 4: Cancel Appointment**
- Click "Cancel Appointment" button on any scheduled/pending appointment
- Confirm the cancellation
- Status updates to "Cancelled" immediately

---

## 🔧 Technical Implementation

### **Frontend (patient-portal.html)**

```javascript
function cancelAppointment(appointmentId) {
    // 1. User confirmation
    if (!confirm('Are you sure you want to cancel this appointment?')) {
        return;
    }

    // 2. Call backend API
    fetch(`/api/appointments/${appointmentId}/cancel?userId=${currentPatientId}&userRole=PATIENT`, {
        method: 'PUT'
    })
    .then(response => response.json())
    .then(data => {
        if (data.error) {
            alert('Error: ' + data.message);
        } else {
            alert('Appointment cancelled successfully!');
            loadAppointments(); // Refresh the list
        }
    });
}
```

### **Backend Flow**

1. **Controller** (`AppointmentController.java`)
   ```java
   @PutMapping("/{id}/cancel")
   public ResponseEntity<?> cancelAppointment(
       @PathVariable Integer id,
       @RequestParam Integer userId,
       @RequestParam String userRole) {  // userRole = "PATIENT"
       // ...
   }
   ```

2. **Service Layer** (`AppointmentService.java`)
   ```java
   @Transactional
   public Appointment cancelAppointment(Integer appointmentId, Integer userId, String userRole) {
       Appointment appointment = appointmentRepository.findById(appointmentId)...;
       
       // Validate ownership
       if ("PATIENT".equalsIgnoreCase(userRole)) {
           if (!appointment.getPatient_id().equals(userId)) {
               throw new RuntimeException("Patient can only cancel their own appointments");
           }
       }
       
       // Validate status
       if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
           throw new RuntimeException("Cannot cancel a completed appointment");
       }
       
       // Update status
       appointment.setStatus("Cancelled");
       return appointmentRepository.save(appointment); // Updates database
   }
   ```

3. **Database Update**
   ```sql
   UPDATE appointments 
   SET status = 'Cancelled' 
   WHERE appointment_id = [appointment_id]
   AND patient_id = [patient_id];  -- Ensures ownership
   ```

---

## 🔒 Security & Validation

### **Ownership Validation**
- Patient ID in request must match `patient_id` in appointment
- Prevents patients from cancelling other patients' appointments

### **Business Rule Validation**
- Cannot cancel completed appointments
- Cannot cancel already cancelled appointments
- Only scheduled/pending appointments can be cancelled

### **Transaction Safety**
- `@Transactional` ensures atomic operations
- If validation fails, no changes are made to database

---

## 🧪 Testing Patient Cancellation

### **Method 1: Using Patient Portal UI**

1. **Get Patient ID:**
   - Register a new patient at `/appoinment.html`
   - Note the Patient ID from the registration response

2. **View Appointments:**
   - Go to `http://localhost:8080/patient-portal.html`
   - Enter Patient ID
   - Click "View My Appointments"

3. **Cancel Appointment:**
   - Find a scheduled appointment
   - Click "Cancel Appointment" button
   - Confirm cancellation
   - Status should update to "Cancelled"

### **Method 2: Using Browser Console**

1. Open `patient-portal.html`
2. Open DevTools (F12) → Console
3. Run:

```javascript
// Replace 5 with actual appointment ID and 1 with actual patient ID
fetch('/api/appointments/5/cancel?userId=1&userRole=PATIENT', {
    method: 'PUT'
})
.then(r => r.json())
.then(console.log);
```

### **Method 3: Using cURL**

```bash
curl -X PUT "http://localhost:8080/api/appointments/5/cancel?userId=1&userRole=PATIENT"
```

---

## 📱 User Interface Screenshots Description

### **Patient Portal Home:**
- Search box to enter Patient ID
- Placeholder text explaining where to find Patient ID

### **Appointments Display:**
- Card-based layout showing each appointment
- Status badge with color coding:
  - 🟢 Green: Scheduled
  - 🔵 Blue: Completed
  - 🔴 Red: Cancelled
  - 🟡 Yellow: Pending
- "Cancel Appointment" button (only for scheduled/pending)

### **After Cancellation:**
- Success message: "Appointment cancelled successfully!"
- List refreshes automatically
- Status badge updates to red "Cancelled"
- Cancel button disappears

---

## 💡 How to Find Patient ID

### **Option 1: From Registration**
- When patient registers at `/appoinment.html`
- Patient ID is returned in the response
- Should be saved or displayed to the patient

### **Option 2: From Database**
```sql
SELECT patient_id, name, email 
FROM patients 
ORDER BY patient_id DESC 
LIMIT 10;
```

### **Option 3: Add to Registration Response**
- Modify registration success message to display Patient ID
- Or email the Patient ID to the patient

---

## 🚀 Quick Start Guide for Patients

1. **Book an Appointment:**
   - Go to `/appoinment.html`
   - Fill out booking form
   - Note your Patient ID after registration (if shown)

2. **Access Patient Portal:**
   - Go to `/patient-portal.html`
   - Or click link on booking page

3. **View Appointments:**
   - Enter your Patient ID
   - Click "View My Appointments"
   - See all your appointments

4. **Cancel if Needed:**
   - Click "Cancel Appointment" on any scheduled appointment
   - Confirm cancellation
   - Appointment is cancelled immediately

---

## ⚠️ Important Notes

1. **No Patient Login Required:** 
   - Currently uses Patient ID lookup (public portal)
   - For production, add patient authentication

2. **Patient ID Security:**
   - Anyone with Patient ID can view that patient's appointments
   - Consider adding password/authentication for production

3. **Cancellation Policy:**
   - Completed appointments cannot be cancelled
   - Cancelled appointments cannot be re-cancelled

4. **Real-time Updates:**
   - Status updates immediately in database
   - Page refreshes to show updated status

---

## 🔄 Complete Flow Diagram

```
Patient Portal Page
    ↓
Enter Patient ID
    ↓
Click "View My Appointments"
    ↓
API: GET /api/appointments/search?patientId=X
    ↓
Display Appointments List
    ↓
Patient Clicks "Cancel Appointment"
    ↓
Confirm Cancellation
    ↓
API: PUT /api/appointments/{id}/cancel?userId=X&userRole=PATIENT
    ↓
Service: Validate ownership & business rules
    ↓
Repository: UPDATE appointments SET status='Cancelled'
    ↓
Database: Row updated
    ↓
Success: Status = "Cancelled"
    ↓
Page refreshes showing updated status
```

---

## 📞 Support

If patients need help:
1. They should have their Patient ID from registration
2. Can use Patient Portal to view/cancel appointments
3. Can book new appointments at `/appoinment.html`

---

**That's how patients can cancel their appointments!** 🎉


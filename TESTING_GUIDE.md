# Appointment Management API - Testing Guide

## Overview
This guide explains how to test the newly implemented appointment management features.

## New Backend Endpoints

### 1. Cancel Appointment
**Endpoint:** `PUT /api/appointments/{id}/cancel`

**Parameters:**
- `userId` (query param): Doctor or Patient ID
- `userRole` (query param): "DOCTOR" or "PATIENT"

**Example:**
```bash
PUT http://localhost:8080/api/appointments/5/cancel?userId=1&userRole=DOCTOR
```

**Response:**
```json
{
  "message": "Appointment cancelled successfully",
  "appointment": { ... }
}
```

### 2. Reschedule Appointment
**Endpoint:** `PUT /api/appointments/{id}/reschedule`

**Parameters:**
- `userId` (query param): Doctor or Patient ID
- `userRole` (query param): "DOCTOR" or "PATIENT"

**Body:**
```json
{
  "appointment_date": "2024-12-25",
  "appointment_time": "14:30"
}
```

**Example:**
```bash
PUT http://localhost:8080/api/appointments/5/reschedule?userId=1&userRole=DOCTOR
Content-Type: application/json

{
  "appointment_date": "2024-12-25",
  "appointment_time": "14:30"
}
```

### 3. Update Appointment Status
**Endpoint:** `PUT /api/appointments/{id}/status`

**Parameters:**
- `userId` (query param): Doctor or Patient ID
- `userRole` (query param): "DOCTOR" or "PATIENT"

**Body:**
```json
{
  "status": "Completed"
}
```

**Valid Status Values:**
- "Scheduled"
- "Completed"
- "Cancelled"
- "Pending"
- "In Progress"

**Example:**
```bash
PUT http://localhost:8080/api/appointments/5/status?userId=1&userRole=DOCTOR
Content-Type: application/json

{
  "status": "Completed"
}
```

### 4. Filter Appointments
**Endpoint:** `GET /api/appointments/search`

**Query Parameters (all optional):**
- `doctorId`: Filter by doctor ID
- `patientId`: Filter by patient ID
- `status`: Filter by status (e.g., "Scheduled", "Completed")
- `startDate`: Filter from this date (YYYY-MM-DD)
- `endDate`: Filter to this date (YYYY-MM-DD)

**Example:**
```bash
# Get all scheduled appointments for doctor 1 in December 2024
GET http://localhost:8080/api/appointments/search?doctorId=1&status=Scheduled&startDate=2024-12-01&endDate=2024-12-31

# Get all appointments for patient 2
GET http://localhost:8080/api/appointments/search?patientId=2

# Get all cancelled appointments in a date range
GET http://localhost:8080/api/appointments/search?status=Cancelled&startDate=2024-01-01&endDate=2024-12-31
```

### 5. Get Current Doctor Info
**Endpoint:** `GET /api/doctors/me`

**Returns:**
```json
{
  "name": "Dr. Sarah Johnson",
  "doctorId": 1,
  "department": "Cardiology"
}
```

## Testing Methods

### Method 1: Using Browser Console (Doctor Dashboard)
1. Log in as a doctor at `/login`
2. Navigate to `/doctor-dashboard`
3. Open browser DevTools (F12)
4. In the Console tab, you can test endpoints:

```javascript
// Get current doctor info
fetch('/api/doctors/me').then(r => r.json()).then(console.log);

// Get all appointments
fetch('/api/doctors/appointments').then(r => r.json()).then(console.log);

// Cancel an appointment (replace 5 with actual appointment ID)
fetch('/api/appointments/5/cancel?userId=1&userRole=DOCTOR', {
    method: 'PUT'
}).then(r => r.json()).then(console.log);

// Update status (replace 5 with actual appointment ID)
fetch('/api/appointments/5/status?userId=1&userRole=DOCTOR', {
    method: 'PUT',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({status: 'Completed'})
}).then(r => r.json()).then(console.log);

// Filter appointments
fetch('/api/appointments/search?doctorId=1&status=Scheduled').then(r => r.json()).then(console.log);
```

### Method 2: Using Postman or cURL

#### Cancel Appointment
```bash
curl -X PUT "http://localhost:8080/api/appointments/5/cancel?userId=1&userRole=DOCTOR"
```

#### Reschedule Appointment
```bash
curl -X PUT "http://localhost:8080/api/appointments/5/reschedule?userId=1&userRole=DOCTOR" \
  -H "Content-Type: application/json" \
  -d '{"appointment_date":"2024-12-25","appointment_time":"14:30"}'
```

#### Update Status
```bash
curl -X PUT "http://localhost:8080/api/appointments/5/status?userId=1&userRole=DOCTOR" \
  -H "Content-Type: application/json" \
  -d '{"status":"Completed"}'
```

#### Filter Appointments
```bash
curl "http://localhost:8080/api/appointments/search?doctorId=1&status=Scheduled"
```

### Method 3: Using the Doctor Dashboard UI
The dashboard now includes action buttons:
1. **Cancel** - Cancel scheduled/pending appointments
2. **Update Status** - Change appointment status (for doctors)

To use:
1. Log in as a doctor
2. View your appointments on the dashboard
3. Click "Cancel" or "Update Status" buttons on any appointment row

## Error Responses

All endpoints return consistent error responses:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message"
}
```

**Common Error Codes:**
- `PATIENT_NOT_FOUND` - Patient doesn't exist
- `OUT_OF_SCHEDULE` - Appointment time outside working hours
- `DAILY_LIMIT_REACHED` - Doctor has reached daily appointment limit
- `CANCELLATION_FAILED` - Cannot cancel (e.g., already cancelled or completed)
- `RESCHEDULE_FAILED` - Cannot reschedule (e.g., past date/time)
- `STATUS_UPDATE_FAILED` - Invalid status transition
- `MISSING_PARAMS` - Required parameters missing
- `NOT_FOUND` - Appointment not found

## Validation Rules

1. **Working Hours:**
   - Mon-Wed: 08:00 - 17:00
   - Thu-Fri: 09:00 - 17:00
   - Sat-Sun: 10:00 - 17:00

2. **Daily Appointment Limits:**
   - General Department: 5 appointments per day
   - All Other Departments: 2 appointments per day

3. **Status Transitions:**
   - Cannot cancel completed appointments
   - Cannot reschedule cancelled appointments
   - Cannot change status of cancelled appointments

4. **Authorization:**
   - Patients can only manage their own appointments
   - Doctors can only manage their own appointments

## Testing Checklist

- [ ] Cancel an appointment as a doctor
- [ ] Cancel an appointment as a patient
- [ ] Try to cancel a completed appointment (should fail)
- [ ] Reschedule an appointment to a valid date/time
- [ ] Try to reschedule to past date (should fail)
- [ ] Try to reschedule outside working hours (should fail)
- [ ] Update appointment status to "Completed"
- [ ] Update appointment status to "In Progress"
- [ ] Filter appointments by doctor ID
- [ ] Filter appointments by patient ID
- [ ] Filter appointments by status
- [ ] Filter appointments by date range
- [ ] Combine multiple filters
- [ ] Test authorization (doctor can't manage other doctor's appointments)
- [ ] View doctor's name on dashboard
- [ ] Test with invalid appointment ID (should return error)


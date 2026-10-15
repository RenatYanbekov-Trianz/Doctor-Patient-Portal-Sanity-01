package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
    }

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_createsNonNullObject() {
        Doctor d = new Doctor();
        assertNotNull(d);
    }

    @Test
    void constructorWithoutId_setsAllFields() {
        Doctor d = new Doctor("Dr. Smith", "1980-05-15", "MBBS", "Cardiology",
                "smith@hospital.com", "9876543210", "pass123");

        assertEquals("Dr. Smith", d.getFullName());
        assertEquals("1980-05-15", d.getDateOfBirth());
        assertEquals("MBBS", d.getQualification());
        assertEquals("Cardiology", d.getSpecialist());
        assertEquals("smith@hospital.com", d.getEmail());
        assertEquals("9876543210", d.getPhone());
        assertEquals("pass123", d.getPassword());
        assertEquals(0, d.getId()); // id not set
    }

    @Test
    void constructorWithId_setsAllFields() {
        Doctor d = new Doctor(1, "Dr. Jones", "1975-08-20", "MD", "Neurology",
                "jones@hospital.com", "1234567890", "secret");

        assertEquals(1, d.getId());
        assertEquals("Dr. Jones", d.getFullName());
        assertEquals("1975-08-20", d.getDateOfBirth());
        assertEquals("MD", d.getQualification());
        assertEquals("Neurology", d.getSpecialist());
        assertEquals("jones@hospital.com", d.getEmail());
        assertEquals("1234567890", d.getPhone());
        assertEquals("secret", d.getPassword());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setAndGetId_returnsCorrectValue() {
        doctor.setId(10);
        assertEquals(10, doctor.getId());
    }

    @Test
    void setAndGetFullName_returnsCorrectValue() {
        doctor.setFullName("Dr. Alice");
        assertEquals("Dr. Alice", doctor.getFullName());
    }

    @Test
    void setAndGetDateOfBirth_returnsCorrectValue() {
        doctor.setDateOfBirth("1990-01-01");
        assertEquals("1990-01-01", doctor.getDateOfBirth());
    }

    @Test
    void setAndGetQualification_returnsCorrectValue() {
        doctor.setQualification("PhD");
        assertEquals("PhD", doctor.getQualification());
    }

    @Test
    void setAndGetSpecialist_returnsCorrectValue() {
        doctor.setSpecialist("Orthopedics");
        assertEquals("Orthopedics", doctor.getSpecialist());
    }

    @Test
    void setAndGetEmail_returnsCorrectValue() {
        doctor.setEmail("doctor@clinic.com");
        assertEquals("doctor@clinic.com", doctor.getEmail());
    }

    @Test
    void setAndGetPhone_returnsCorrectValue() {
        doctor.setPhone("5559876543");
        assertEquals("5559876543", doctor.getPhone());
    }

    @Test
    void setAndGetPassword_returnsCorrectValue() {
        doctor.setPassword("mySecurePass");
        assertEquals("mySecurePass", doctor.getPassword());
    }

    // ── Edge Case Tests ────────────────────────────────────────────────────────

    @Test
    void setFullName_withNull_storesNull() {
        doctor.setFullName(null);
        assertNull(doctor.getFullName());
    }

    @Test
    void setEmail_withEmptyString_storesEmpty() {
        doctor.setEmail("");
        assertEquals("", doctor.getEmail());
    }

    @Test
    void setId_withZero_returnsZero() {
        doctor.setId(0);
        assertEquals(0, doctor.getId());
    }

    @Test
    void setId_withNegativeValue_returnsNegative() {
        doctor.setId(-5);
        assertEquals(-5, doctor.getId());
    }

    @Test
    void setPassword_withEmptyString_storesEmpty() {
        doctor.setPassword("");
        assertEquals("", doctor.getPassword());
    }

    @Test
    void defaultConstructor_idDefaultsToZero() {
        Doctor d = new Doctor();
        assertEquals(0, d.getId());
    }

    @Test
    void setSpecialist_withNull_storesNull() {
        doctor.setSpecialist(null);
        assertNull(doctor.getSpecialist());
    }

    @Test
    void setQualification_withNull_storesNull() {
        doctor.setQualification(null);
        assertNull(doctor.getQualification());
    }
}

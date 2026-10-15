package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecialistTest {

    private Specialist specialist;

    @BeforeEach
    void setUp() {
        specialist = new Specialist();
    }

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_createsNonNullObject() {
        Specialist s = new Specialist();
        assertNotNull(s);
    }

    @Test
    void parameterizedConstructor_setsIdAndName() {
        Specialist s = new Specialist(1, "Cardiology");
        assertEquals(1, s.getId());
        assertEquals("Cardiology", s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_withZeroId_setsZeroId() {
        Specialist s = new Specialist(0, "General");
        assertEquals(0, s.getId());
        assertEquals("General", s.getSpecialistName());
    }

    @Test
    void parameterizedConstructor_withNullName_storesNull() {
        Specialist s = new Specialist(5, null);
        assertEquals(5, s.getId());
        assertNull(s.getSpecialistName());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setAndGetId_returnsCorrectValue() {
        specialist.setId(10);
        assertEquals(10, specialist.getId());
    }

    @Test
    void setAndGetSpecialistName_returnsCorrectValue() {
        specialist.setSpecialistName("Neurology");
        assertEquals("Neurology", specialist.getSpecialistName());
    }

    @Test
    void setId_withNegativeValue_returnsNegative() {
        specialist.setId(-3);
        assertEquals(-3, specialist.getId());
    }

    @Test
    void setSpecialistName_withNull_storesNull() {
        specialist.setSpecialistName(null);
        assertNull(specialist.getSpecialistName());
    }

    @Test
    void setSpecialistName_withEmptyString_storesEmpty() {
        specialist.setSpecialistName("");
        assertEquals("", specialist.getSpecialistName());
    }

    @Test
    void defaultConstructor_idDefaultsToZero() {
        Specialist s = new Specialist();
        assertEquals(0, s.getId());
    }

    @Test
    void defaultConstructor_nameDefaultsToNull() {
        Specialist s = new Specialist();
        assertNull(s.getSpecialistName());
    }

    @Test
    void setId_withLargeValue_returnsLargeValue() {
        specialist.setId(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, specialist.getId());
    }

    @Test
    void setSpecialistName_withLongString_storesCorrectly() {
        String longName = "Cardiothoracic and Vascular Surgery";
        specialist.setSpecialistName(longName);
        assertEquals(longName, specialist.getSpecialistName());
    }
}

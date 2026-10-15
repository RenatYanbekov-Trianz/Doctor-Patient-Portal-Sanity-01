package com.hms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // ── Constructor Tests ──────────────────────────────────────────────────────

    @Test
    void defaultConstructor_createsNonNullObject() {
        User u = new User();
        assertNotNull(u);
    }

    @Test
    void constructorWithIdFullNameEmailPassword_setsAllFields() {
        User u = new User(1, "Alice", "alice@example.com", "pass123");
        assertEquals(1, u.getId());
        assertEquals("Alice", u.getFullName());
        assertEquals("alice@example.com", u.getEmail());
        assertEquals("pass123", u.getPassword());
    }

    @Test
    void constructorWithoutId_setsFieldsExceptId() {
        User u = new User("Bob", "bob@example.com", "secret");
        assertEquals(0, u.getId()); // id not set, defaults to 0
        assertEquals("Bob", u.getFullName());
        assertEquals("bob@example.com", u.getEmail());
        assertEquals("secret", u.getPassword());
    }

    // ── Getter / Setter Tests ──────────────────────────────────────────────────

    @Test
    void setAndGetId_returnsCorrectValue() {
        user.setId(5);
        assertEquals(5, user.getId());
    }

    @Test
    void setAndGetFullName_returnsCorrectValue() {
        user.setFullName("Charlie");
        assertEquals("Charlie", user.getFullName());
    }

    @Test
    void setAndGetEmail_returnsCorrectValue() {
        user.setEmail("charlie@test.com");
        assertEquals("charlie@test.com", user.getEmail());
    }

    @Test
    void setAndGetPassword_returnsCorrectValue() {
        user.setPassword("myPassword");
        assertEquals("myPassword", user.getPassword());
    }

    // ── toString Tests ─────────────────────────────────────────────────────────

    @Test
    void toString_containsId() {
        user.setId(3);
        user.setFullName("Dave");
        user.setEmail("dave@test.com");
        user.setPassword("pw");
        assertTrue(user.toString().contains("3"));
    }

    @Test
    void toString_containsFullName() {
        user.setFullName("Eve");
        assertTrue(user.toString().contains("Eve"));
    }

    @Test
    void toString_containsEmail() {
        user.setEmail("eve@test.com");
        assertTrue(user.toString().contains("eve@test.com"));
    }

    @Test
    void toString_containsPassword() {
        user.setPassword("pw123");
        assertTrue(user.toString().contains("pw123"));
    }

    @Test
    void toString_returnsExpectedFormat() {
        User u = new User(1, "Alice", "alice@example.com", "pass123");
        String expected = "User [id=1, fullName=Alice, email=alice@example.com, password=pass123]";
        assertEquals(expected, u.toString());
    }

    // ── Edge Case Tests ────────────────────────────────────────────────────────

    @Test
    void setFullName_withNull_storesNull() {
        user.setFullName(null);
        assertNull(user.getFullName());
    }

    @Test
    void setEmail_withEmptyString_storesEmpty() {
        user.setEmail("");
        assertEquals("", user.getEmail());
    }

    @Test
    void setPassword_withNull_storesNull() {
        user.setPassword(null);
        assertNull(user.getPassword());
    }

    @Test
    void setId_withZero_returnsZero() {
        user.setId(0);
        assertEquals(0, user.getId());
    }

    @Test
    void setId_withNegativeValue_returnsNegative() {
        user.setId(-10);
        assertEquals(-10, user.getId());
    }

    @Test
    void defaultConstructor_idDefaultsToZero() {
        User u = new User();
        assertEquals(0, u.getId());
    }
}

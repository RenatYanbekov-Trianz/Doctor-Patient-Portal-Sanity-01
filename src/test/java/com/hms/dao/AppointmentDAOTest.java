package com.hms.dao;

import com.hms.entity.Appointment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private AppointmentDAO appointmentDAO;

    @BeforeEach
    void setUp() {
        appointmentDAO = new AppointmentDAO(mockConnection);
    }

    // ── Constructor Test ───────────────────────────────────────────────────────

    @Test
    void constructor_withConnection_createsInstance() {
        AppointmentDAO dao = new AppointmentDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── addAppointment Tests ───────────────────────────────────────────────────

    @Test
    void addAppointment_success_returnsTrue() throws Exception {
        Appointment appointment = new Appointment(1, "John", "Male", "30",
                "2024-01-15", "john@test.com", "1234567890", "Fever", 2, "123 St", "Pending");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.addAppointment(appointment);

        assertTrue(result);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void addAppointment_whenExceptionThrown_returnsFalse() throws Exception {
        Appointment appointment = new Appointment(1, "John", "Male", "30",
                "2024-01-15", "john@test.com", "1234567890", "Fever", 2, "123 St", "Pending");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = appointmentDAO.addAppointment(appointment);

        assertFalse(result);
    }

    // ── getAllAppointmentByLoginUser Tests ─────────────────────────────────────

    @Test
    void getAllAppointmentByLoginUser_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getInt(2)).thenReturn(10);
        when(mockResultSet.getString(3)).thenReturn("John Doe");
        when(mockResultSet.getString(4)).thenReturn("Male");
        when(mockResultSet.getString(5)).thenReturn("30");
        when(mockResultSet.getString(6)).thenReturn("2024-01-15");
        when(mockResultSet.getString(7)).thenReturn("john@test.com");
        when(mockResultSet.getString(8)).thenReturn("1234567890");
        when(mockResultSet.getString(9)).thenReturn("Fever");
        when(mockResultSet.getInt(10)).thenReturn(2);
        when(mockResultSet.getString(11)).thenReturn("123 St");
        when(mockResultSet.getString(12)).thenReturn("Pending");

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
    }

    @Test
    void getAllAppointmentByLoginUser_withNoResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(99);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointmentByLoginUser_whenExceptionThrown_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginUser(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getAllAppointmentByLoginDoctor Tests ───────────────────────────────────

    @Test
    void getAllAppointmentByLoginDoctor_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(5);
        when(mockResultSet.getInt(2)).thenReturn(10);
        when(mockResultSet.getString(3)).thenReturn("Jane Doe");
        when(mockResultSet.getString(4)).thenReturn("Female");
        when(mockResultSet.getString(5)).thenReturn("25");
        when(mockResultSet.getString(6)).thenReturn("2024-02-20");
        when(mockResultSet.getString(7)).thenReturn("jane@test.com");
        when(mockResultSet.getString(8)).thenReturn("9876543210");
        when(mockResultSet.getString(9)).thenReturn("Cold");
        when(mockResultSet.getInt(10)).thenReturn(3);
        when(mockResultSet.getString(11)).thenReturn("456 Oak Ave");
        when(mockResultSet.getString(12)).thenReturn("Approved");

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(3);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Jane Doe", result.get(0).getFullName());
    }

    @Test
    void getAllAppointmentByLoginDoctor_withNoResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(99);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointmentByLoginDoctor_whenExceptionThrown_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Appointment> result = appointmentDAO.getAllAppointmentByLoginDoctor(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getAppointmentById Tests ───────────────────────────────────────────────

    @Test
    void getAppointmentById_withValidId_returnsAppointment() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(1);
        when(mockResultSet.getInt(2)).thenReturn(10);
        when(mockResultSet.getString(3)).thenReturn("John Doe");
        when(mockResultSet.getString(4)).thenReturn("Male");
        when(mockResultSet.getString(5)).thenReturn("30");
        when(mockResultSet.getString(6)).thenReturn("2024-01-15");
        when(mockResultSet.getString(7)).thenReturn("john@test.com");
        when(mockResultSet.getString(8)).thenReturn("1234567890");
        when(mockResultSet.getString(9)).thenReturn("Fever");
        when(mockResultSet.getInt(10)).thenReturn(2);
        when(mockResultSet.getString(11)).thenReturn("123 St");
        when(mockResultSet.getString(12)).thenReturn("Pending");

        Appointment result = appointmentDAO.getAppointmentById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John Doe", result.getFullName());
    }

    @Test
    void getAppointmentById_withNoResult_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Appointment result = appointmentDAO.getAppointmentById(999);

        assertNull(result);
    }

    @Test
    void getAppointmentById_whenExceptionThrown_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        Appointment result = appointmentDAO.getAppointmentById(1);

        assertNull(result);
    }

    // ── updateDrAppointmentCommentStatus Tests ─────────────────────────────────

    @Test
    void updateDrAppointmentCommentStatus_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = appointmentDAO.updateDrAppointmentCommentStatus(1, 2, "Approved");

        assertTrue(result);
    }

    @Test
    void updateDrAppointmentCommentStatus_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = appointmentDAO.updateDrAppointmentCommentStatus(1, 2, "Approved");

        assertFalse(result);
    }

    // ── getAllAppointment Tests ─────────────────────────────────────────────────

    @Test
    void getAllAppointment_withResults_returnsPopulatedList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt(1)).thenReturn(1, 2);
        when(mockResultSet.getInt(2)).thenReturn(10, 11);
        when(mockResultSet.getString(3)).thenReturn("John", "Jane");
        when(mockResultSet.getString(4)).thenReturn("Male", "Female");
        when(mockResultSet.getString(5)).thenReturn("30", "25");
        when(mockResultSet.getString(6)).thenReturn("2024-01-15", "2024-02-20");
        when(mockResultSet.getString(7)).thenReturn("john@test.com", "jane@test.com");
        when(mockResultSet.getString(8)).thenReturn("1234567890", "9876543210");
        when(mockResultSet.getString(9)).thenReturn("Fever", "Cold");
        when(mockResultSet.getInt(10)).thenReturn(2, 3);
        when(mockResultSet.getString(11)).thenReturn("123 St", "456 Ave");
        when(mockResultSet.getString(12)).thenReturn("Pending", "Approved");

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAllAppointment_withNoResults_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllAppointment_whenExceptionThrown_returnsEmptyList() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        List<Appointment> result = appointmentDAO.getAllAppointment();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

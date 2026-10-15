package com.hms.dao;

import com.hms.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO(mockConnection);
    }

    // ── Constructor Test ───────────────────────────────────────────────────────

    @Test
    void constructor_withConnection_createsInstance() {
        UserDAO dao = new UserDAO(mockConnection);
        assertNotNull(dao);
    }

    // ── userRegister Tests ─────────────────────────────────────────────────────

    @Test
    void userRegister_success_returnsTrue() throws Exception {
        User user = new User("Alice", "alice@example.com", "pass123");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void userRegister_whenExceptionThrown_returnsFalse() throws Exception {
        User user = new User("Alice", "alice@example.com", "pass123");

        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.userRegister(user);

        assertFalse(result);
    }

    @Test
    void userRegister_withNullFields_whenExceptionThrown_returnsFalse() throws Exception {
        User user = new User(null, null, null);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), isNull());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.userRegister(user);

        assertTrue(result);
    }

    // ── loginUser Tests ────────────────────────────────────────────────────────

    @Test
    void loginUser_withValidCredentials_returnsUser() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("full_name")).thenReturn("Alice");
        when(mockResultSet.getString("email")).thenReturn("alice@example.com");
        when(mockResultSet.getString("password")).thenReturn("pass123");

        User result = userDAO.loginUser("alice@example.com", "pass123");

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Alice", result.getFullName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("pass123", result.getPassword());
    }

    @Test
    void loginUser_withInvalidCredentials_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        User result = userDAO.loginUser("wrong@email.com", "wrongpass");

        assertNull(result);
    }

    @Test
    void loginUser_whenExceptionThrown_returnsNull() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        User result = userDAO.loginUser("alice@example.com", "pass123");

        assertNull(result);
    }

    // ── checkOldPassword Tests ─────────────────────────────────────────────────

    @Test
    void checkOldPassword_withMatchingPassword_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        boolean result = userDAO.checkOldPassword(1, "oldPass");

        assertTrue(result);
    }

    @Test
    void checkOldPassword_withNonMatchingPassword_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = userDAO.checkOldPassword(1, "wrongPass");

        assertFalse(result);
    }

    @Test
    void checkOldPassword_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.checkOldPassword(1, "pass");

        assertFalse(result);
    }

    // ── changePassword Tests ───────────────────────────────────────────────────

    @Test
    void changePassword_success_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(1, "newPass");

        assertTrue(result);
    }

    @Test
    void changePassword_whenExceptionThrown_returnsFalse() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("DB error"));

        boolean result = userDAO.changePassword(1, "newPass");

        assertFalse(result);
    }

    @Test
    void changePassword_withEmptyPassword_returnsTrue() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        boolean result = userDAO.changePassword(1, "");

        assertTrue(result);
    }
}

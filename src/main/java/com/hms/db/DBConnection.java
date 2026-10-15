package com.hms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	// Note: Static Connection field removed - not thread-safe in Java 21 servlet environment.
	// Each call now creates a new connection. For production use, consider a connection pool
	// (e.g., HikariCP or Tomcat JDBC Pool).

	private static final String JDBC_URL = "jdbc:mysql://localhost:3306/hospital";
	private static final String JDBC_USER = "root";
	private static final String JDBC_PASSWORD = "wasim";

	/**
	 * Returns a new database connection.
	 * Note: Class.forName() is no longer required in Java 21 as JDBC 4.0+ drivers
	 * are auto-loaded via ServiceLoader. The mysql-connector-j driver registers itself.
	 *
	 * @return a new {@link Connection} instance, or null if connection fails
	 */
	public static Connection getConn() {
		try {
			// JDBC 4.0+ (Java 6+): Driver auto-registration via ServiceLoader.
			// Class.forName("com.mysql.cj.jdbc.Driver") is no longer needed in Java 21.
			return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}

package com.hms.entity;

/**
 * User entity class representing a patient/user in the Doctor-Patient Portal.
 *
 * <p><strong>Cloud Security Note (cr-java-0113 — Lack of Externalized Secrets):</strong><br>
 * The {@code password} field is intentionally excluded from {@link #toString()} to prevent
 * credential exposure in application logs, stack traces, and string representations.
 * This follows AWS cloud security best practices and the 12-factor app principle of
 * treating credentials as secrets that must never appear in log output.</p>
 *
 * <p>Passwords and other sensitive credentials must be managed via
 * AWS Secrets Manager (see {@code com.hms.util.AwsSecretsManagerUtil}) rather than
 * being embedded in source code or surfaced through diagnostic output.</p>
 */
public class User {

	private int id;
	private String fullName;
	private String email;
	// NOTE: password is stored as a field for authentication purposes only.
	// It must NEVER be logged, serialised to JSON responses, or included in toString().
	private String password;


	public User() {
		super();
	}


	public User(int id, String fullName, String email, String password) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.password = password;
	}


	public User(String fullName, String email, String password) {
		super();
		this.fullName = fullName;
		this.email = email;
		this.password = password;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getFullName() {
		return fullName;
	}


	public void setFullName(String fullName) {
		this.fullName = fullName;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Returns a string representation of this User.
	 *
	 * <p><strong>Security:</strong> The {@code password} field is intentionally omitted
	 * to prevent credential exposure in logs and diagnostic output (cr-java-0113).
	 * Credentials must be managed via AWS Secrets Manager, not surfaced in plain text.</p>
	 *
	 * @return a safe string representation containing only non-sensitive fields
	 */
	@Override
	public String toString() {
		// Password is intentionally omitted to prevent credential exposure in logs.
		// Remediation for cr-java-0113: Lack of Externalized Secrets — AWS Secrets Manager.
		return "User [id=" + id + ", fullName=" + fullName + ", email=" + email + "]";
	}
}

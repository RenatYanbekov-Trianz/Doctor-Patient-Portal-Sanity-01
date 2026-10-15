package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * User logout servlet.
 *
 * Session management is handled by Spring Session with Amazon ElastiCache for Redis,
 * enabling stateless application instances with centralized, distributed session
 * management. Session invalidation is propagated to Redis, ensuring all instances
 * recognize the logout immediately.
 */
@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Spring Session transparently manages this session in Amazon ElastiCache for Redis
		HttpSession session = req.getSession();
		// Removing attribute from Redis-backed session - propagated across all instances
		session.removeAttribute("userObj");
		// Success message stored in Redis-backed session
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");
		
	}

	
	
}

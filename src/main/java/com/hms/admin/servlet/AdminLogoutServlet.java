package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Admin logout servlet.
 *
 * Session management is handled by Spring Session with Amazon ElastiCache for Redis,
 * enabling stateless application instances with centralized, distributed session
 * management. Session invalidation is propagated to Redis, ensuring all instances
 * recognize the logout immediately.
 */
@WebServlet("/adminLogout")
public class AdminLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Spring Session transparently manages this session in Amazon ElastiCache for Redis
		// get session means get "adminObj" and remove it, logout done!
		HttpSession session = req.getSession();
		// Removing attribute from Redis-backed session - propagated across all instances
		session.removeAttribute("adminObj");
		// show message after logout - stored in Redis-backed session
		session.setAttribute("successMsg", "Admin Logout Successfully");
		resp.sendRedirect("admin_login.jsp");
		
		
		
	}

	
}

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
 * <p>cr-java-0065 (HTTP Session State Storage): Session state is now backed by
 * Amazon ElastiCache for Redis via Spring Session (see
 * {@code com.hms.config.RedisSessionConfig} and the
 * {@code springSessionRepositoryFilter} in {@code web.xml}).  The standard
 * {@link HttpSession} API is preserved; Spring Session transparently
 * serialises all session attributes to Redis, enabling stateless instances
 * and horizontal scaling without server affinity.
 */
@WebServlet("/adminLogout")
public class AdminLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// Spring Session transparently removes this attribute from Amazon ElastiCache for Redis
		HttpSession session = req.getSession();
		session.removeAttribute("adminObj");
		//show message after logout
		session.setAttribute("successMsg", "Admin Logout Successfully");
		resp.sendRedirect("admin_login.jsp");
		
		
		
	}

	
}

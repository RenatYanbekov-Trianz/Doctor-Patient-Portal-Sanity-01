package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.SpecialistDAO;
import com.hms.db.DBConnection;

/**
 * Add specialist servlet.
 *
 * Session management is handled by Spring Session with Amazon ElastiCache for Redis,
 * enabling stateless application instances with centralized, distributed session
 * management. The HttpSession API is preserved; Spring Session transparently
 * redirects all session operations to Redis instead of in-memory storage.
 */
@WebServlet("/addSpecialist")
public class SpecialistServlet extends HttpServlet{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String specialistName = req.getParameter("specialistName");
		
		SpecialistDAO specialistDAO = new SpecialistDAO(DBConnection.getConn());
		boolean f = specialistDAO.addSpecialist(specialistName);
		
		// Spring Session transparently stores this session in Amazon ElastiCache for Redis
		HttpSession session = req.getSession();
		
		if (f==true) {
			// Success message stored in Redis-backed session - visible across all instances
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");
			
		} else {
			// Error message stored in Redis-backed session
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}
	
	

}

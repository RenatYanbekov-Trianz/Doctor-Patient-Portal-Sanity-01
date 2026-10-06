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
 * <p>cr-java-0065 (HTTP Session State Storage): Session state is now backed by
 * Amazon ElastiCache for Redis via Spring Session (see
 * {@code com.hms.config.RedisSessionConfig} and the
 * {@code springSessionRepositoryFilter} in {@code web.xml}).  The standard
 * {@link HttpSession} API is preserved; Spring Session transparently
 * serialises all session attributes to Redis, enabling stateless instances
 * and horizontal scaling without server affinity.
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
			session.setAttribute("successMsg", "Specialist added Successfully.");
			resp.sendRedirect("admin/index.jsp");
			
		} else {
			session.setAttribute("errorMsg", "Something went wrong on server");
			resp.sendRedirect("admin/index.jsp");
		}
	}
	
	

}

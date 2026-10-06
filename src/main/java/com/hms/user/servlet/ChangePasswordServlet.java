package com.hms.user.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;

/**
 * Change password servlet.
 *
 * <p>cr-java-0065 (HTTP Session State Storage): Session state is now backed by
 * Amazon ElastiCache for Redis via Spring Session (see
 * {@code com.hms.config.RedisSessionConfig} and the
 * {@code springSessionRepositoryFilter} in {@code web.xml}).  The standard
 * {@link HttpSession} API is preserved; Spring Session transparently
 * serialises all session attributes to Redis, enabling stateless instances
 * and horizontal scaling without server affinity.
 */
@WebServlet("/userChangePassword")
public class ChangePasswordServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int userId = Integer.parseInt(req.getParameter("userId"));
		String oldPassword = req.getParameter("oldPassword");
		String newPassword = req.getParameter("newPassword");

		UserDAO uDAO = new UserDAO(DBConnection.getConn());

		// Spring Session transparently stores this session in Amazon ElastiCache for Redis
		HttpSession session = req.getSession();

		if (uDAO.checkOldPassword(userId, oldPassword)) {

			if (uDAO.changePassword(userId, newPassword)) {
				session.setAttribute("successMsg", "Password Change Successfully.");
				resp.sendRedirect("change_password.jsp");

			} else {
				session.setAttribute("errorMsg", "Something wrong on server!");
				resp.sendRedirect("change_password.jsp");
			}

		} else {
			session.setAttribute("errorMsg", "Old password incorrect");
			resp.sendRedirect("change_password.jsp");
		}
	}

}

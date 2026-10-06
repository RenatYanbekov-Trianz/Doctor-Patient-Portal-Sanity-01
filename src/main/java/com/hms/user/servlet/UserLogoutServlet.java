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
 * <p>cr-java-0065 (HTTP Session State Storage): Session state is now backed by
 * Amazon ElastiCache for Redis via Spring Session (see
 * {@code com.hms.config.RedisSessionConfig} and the
 * {@code springSessionRepositoryFilter} in {@code web.xml}).  The standard
 * {@link HttpSession} API is preserved; Spring Session transparently
 * serialises all session attributes to Redis, enabling stateless instances
 * and horizontal scaling without server affinity.  On logout,
 * {@code session.removeAttribute()} removes the attribute from the
 * centralised Redis store, ensuring the session is invalidated across all
 * application instances.
 */
@WebServlet("/userLogout")
public class UserLogoutServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// cr-java-0065: HttpSession is transparently backed by Amazon ElastiCache for
		// Redis via Spring Session (springSessionRepositoryFilter in web.xml +
		// RedisSessionConfig). All setAttribute/removeAttribute calls operate on the
		// centralised Redis store, enabling stateless instances and horizontal scaling.
		HttpSession session = req.getSession();
		session.removeAttribute("userObj");
		session.setAttribute("successMsg", "User Logout Successfully.");
		resp.sendRedirect("user_login.jsp");
	}

}

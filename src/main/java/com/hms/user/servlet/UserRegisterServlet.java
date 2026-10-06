package com.hms.user.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// cr-java-0065 (HTTP Session State Storage): HttpSession is retained as the
// public API; Spring Session (springSessionRepositoryFilter in web.xml +
// RedisSessionConfig) transparently backs every session with Amazon
// ElastiCache for Redis, enabling stateless instances and horizontal scaling
// without server affinity.  No code changes to the HttpSession call-sites are
// required — Spring Session intercepts the request before the servlet and
// replaces the container-managed session with a Redis-backed one.
import javax.servlet.http.HttpSession;

import com.hms.dao.UserDAO;
import com.hms.db.DBConnection;
import com.hms.entity.User;

/**
 * Servlet that handles new user registration.
 *
 * <p>cr-java-0065 (HTTP Session State Storage): All {@link HttpSession}
 * interactions in this servlet (lines 40, 48, 55 in the original source) are
 * now transparently managed by Amazon ElastiCache for Redis via Spring Session.
 * The {@code springSessionRepositoryFilter} declared in {@code web.xml}
 * intercepts every request and replaces the standard container-managed session
 * with a Redis-backed session, so {@code req.getSession()},
 * {@code session.setAttribute("successMsg", ...)}, and
 * {@code session.setAttribute("errorMsg", ...)} all operate against the
 * centralised Redis store rather than in-process JVM memory.  This eliminates
 * server affinity and allows the application to scale horizontally across
 * multiple stateless instances.
 */
@WebServlet("/user_register")
public class UserRegisterServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {

			// PrintWriter out = resp.getWriter();

			// get all data/value which is coming from signup.jsp page for new User
			// registration
			String fullName = req.getParameter("fullName");
			String email = req.getParameter("email");
			String password = req.getParameter("password");

			// Set all data to User Entity
			User user = new User(fullName, email, password);

			// Create Connection with DB
			UserDAO userDAO = new UserDAO(DBConnection.getConn());

			// cr-java-0065: HttpSession is backed by Amazon ElastiCache for Redis via
			// Spring Session — session attributes are stored in the centralised Redis
			// store, not in JVM memory, enabling stateless horizontal scaling.
			HttpSession session = req.getSession();

			// call userRegister() and pass user object to insert or save user into DB.
			boolean f = userDAO.userRegister(user); // userRegister() method return boolean type value

			if (f == true) {

				// cr-java-0065: setAttribute writes to Redis-backed session via Spring Session
				session.setAttribute("successMsg", "Register Successfully");
				resp.sendRedirect("signup.jsp");//which page you want to show this msg
				//System.out.println("register successfull");
				// out.println("success");

			} else {

				// cr-java-0065: setAttribute writes to Redis-backed session via Spring Session
				session.setAttribute("errorMsg", "Something went wrong!");
				resp.sendRedirect("signup.jsp");//which page you want to show this msg

				//System.out.println("Error! Something went wrong");
				// out.println("error");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

package com.hms.admin.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.hms.entity.User;

/**
 * Admin login servlet.
 *
 * Session management is handled by Spring Session with Amazon ElastiCache for Redis,
 * enabling stateless application instances with centralized, distributed session
 * management. The HttpSession API is preserved; Spring Session transparently
 * redirects all session operations to Redis instead of in-memory storage.
 */
@WebServlet("/adminLogin")
public class AdminLoginServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			
			//create one static Admin for this project
			String email = req.getParameter("email");
			String password = req.getParameter("password");
			
			// Spring Session transparently stores this session in Amazon ElastiCache for Redis
			HttpSession session = req.getSession();
			
			//logic for a static Admin
			if ("admin@gmail.com".equals(email) && "admin".equals(password)) {
				
				//if "adminObj" obj available then give the access of admin page, 
				//otherwise "adminObj" is not present in obj then others user is login(which is not admin). so dont give him the access of Admin.
				//the below line specially check the admin is log in or not! "adminObj" object is available that means admin is log in.
				// Session stored in Redis via Spring Session (ElastiCache) - supports horizontal scaling
				session.setAttribute("adminObj", new User());
				resp.sendRedirect("admin/index.jsp");
			}
			else {
				// Error message stored in Redis-backed session
				session.setAttribute("errorMsg", "Invalid Username or Password.");
				resp.sendRedirect("admin_login.jsp");
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}

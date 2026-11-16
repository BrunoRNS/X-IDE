package org.xide.http.controllers;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xide.http.service.LoadPdfUsers;
import org.xide.models.User;
import org.xide.repository.UserRepo;

import io.javalin.http.Context;

public class StudentsController {

	private static final Logger LOGGER = Logger.getLogger(StudentsController.class.getName());

	public static void index(Context ctx) {
		List<User> students;
		try {
			students = UserRepo.findAll();
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, "Could not load students", e);
			students = List.of();
		}
		ctx.render("templates/students.html", Map.of("students", students));
	}

	public static void loadPdf(Context ctx){
		byte[] bytes = LoadPdfUsers.selectUsers();
		ctx.contentType("application/pdf");
		ctx.header("Content-Disposition", "inline; filename=users.pdf");
		ctx.result(new ByteArrayInputStream(bytes));

	}

}

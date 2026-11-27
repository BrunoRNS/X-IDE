package org.xide.http.controllers;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.javalin.http.Context;
import org.xide.http.service.LoadPdfUsers;
import org.xide.models.User;
import org.xide.repository.UserRepo;

/**
 * HTTP controller for students/users: list page, PDF export of users
 * ({@link org.xide.http.service.LoadPdfUsers}), registration, update, and deletion via
 * {@link org.xide.repository.UserRepo}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class StudentsController {

	/**
	 * Request body for registering or updating a user: name and email.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record UserDTO(String name, String email) {
	}

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

	public static void loadPdf(Context ctx) {
		byte[] bytes = LoadPdfUsers.selectUsers();
		ctx.contentType("application/pdf");
		ctx.header("Content-Disposition", "inline; filename=users.pdf");
		ctx.result(new ByteArrayInputStream(bytes));

	}

	public static void register(Context ctx) {
		try {
			UserDTO dto = ctx.bodyAsClass(UserDTO.class);
			String password = dto.name() + dto.email().substring(2);
			User savedUser = UserRepo.save(dto.email(), password, dto.name());

			ctx.status(201).json(savedUser);
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not register user", e);
			ctx.status(500);
		}
	}

	public static void deleteUser(Context ctx) {
		Long id = Long.valueOf(ctx.pathParam("id"));
		try {
			if (UserRepo.deleteById(id)) {
				ctx.status(200);
			}
			else {
				ctx.status(404);
			}
		}
		catch (Exception e) {
			ctx.status(500);
		}
	}

	public static void updateUser(Context ctx) {
		Long id = Long.valueOf(ctx.pathParam("id"));
		UserDTO dto = ctx.bodyAsClass(UserDTO.class);
		String password = dto.name() + dto.email().substring(2);
		User user = new User(dto.name(), password, dto.email(), id);
		try {
			if (UserRepo.update(user)) {
				ctx.status(200);
			}
			else {
				ctx.status(404);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			ctx.status(500);
		}
	}

}

package org.xide.http.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.xide.models.User;
import org.xide.repository.UserRepo;

public class LoadPdfUsers {

	public static byte[] selectUsers() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document();

		try {
			PdfWriter.getInstance(document, out);
			document.open();
			List<User> users = UserRepo.findAll();
			for (User user : users) {
				document.add(new Paragraph("Email: " + user.getEmail() + " password " + user.getPassword() + " id "
						+ user.getId() + " name: " + user.getName()));
			}
			document.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

}

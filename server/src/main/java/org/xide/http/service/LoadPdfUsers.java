
package org.xide.http.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.xide.models.User;
import org.xide.repository.UserRepo;

/**
 * Generates an in-memory PDF (OpenPDF) with one card per user (name, email, id, password)
 * for printing/download.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class LoadPdfUsers {

	public static byte[] selectUsers() {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document();

		try {
			PdfWriter.getInstance(document, out);
			document.open();

			List<User> users = UserRepo.findAll();

			Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD);
			Font textFont = new Font(Font.HELVETICA, 11);

			for (User user : users) {

				PdfPTable card = new PdfPTable(1);
				card.setWidthPercentage(100);
				card.setSpacingAfter(15);

				Paragraph title = new Paragraph(user.getName(), titleFont);

				Paragraph email = new Paragraph("Email: " + user.getEmail(), textFont);

				Paragraph id = new Paragraph("ID: " + user.getId(), textFont);
				Paragraph password = new Paragraph("password: " + user.getPassword(), textFont);

				PdfPCell cell = new PdfPCell();

				cell.addElement(title);
				cell.addElement(email);
				cell.addElement(id);
				cell.addElement(password);

				cell.setPadding(15);
				cell.setBorderWidth(1);
				cell.setBorderColor(Color.LIGHT_GRAY);
				cell.setBackgroundColor(new Color(245, 245, 245));

				card.addCell(cell);

				document.add(card);
			}

			document.close();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return out.toByteArray();
	}

}

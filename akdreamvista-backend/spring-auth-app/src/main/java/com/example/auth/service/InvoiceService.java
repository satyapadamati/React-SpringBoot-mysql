package com.example.auth.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.auth.entity.Property;
import com.example.auth.model.PropertyPayment;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

@Service
public class InvoiceService {
	private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

	public byte[] generateInvoice(Property property, PropertyPayment payment) {
		logger.info("generateInvoice is coming {}");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4, 36, 36, 36, 36);

		try {
			PdfWriter.getInstance(document, out);
			document.open();

			Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(255, 120, 0));
			Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font normalFont = new Font(Font.HELVETICA, 11);

			// 🔥 HEADER
			Paragraph title = new Paragraph("AK DREAM VISTA\n", titleFont);
			title.setAlignment(Element.ALIGN_LEFT);
			document.add(title);

			document.add(new Paragraph("Hyderabad - Telangana\n+91 8328041624\ninfo@akdreamvista.com\n\n", normalFont));

			document.add(new LineSeparator());

			// 🔹 Invoice Info
			PdfPTable infoTable = new PdfPTable(2);
			infoTable.setWidthPercentage(100);
			infoTable.setSpacingBefore(15);

			infoTable.addCell(cell("Invoice No:", headerFont));
			infoTable.addCell(cell("AKD-" + payment.getId(), normalFont));

			infoTable.addCell(cell("Invoice Date:", headerFont));
			infoTable.addCell(
					cell(payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), normalFont));

			document.add(infoTable);

			// 🔹 Property Info
			PdfPTable propTable = new PdfPTable(2);
			propTable.setWidthPercentage(100);
			propTable.setSpacingBefore(15);

			propTable.addCell(cell("Property ID:", headerFont));
			propTable.addCell(cell(String.valueOf(property.getId()), normalFont));

			propTable.addCell(cell("Property Title:", headerFont));
			propTable.addCell(cell(property.getPropertiesTitle(), normalFont));

			document.add(propTable);

			// 🔹 Service Table
			PdfPTable serviceTable = new PdfPTable(2);
			serviceTable.setWidthPercentage(100);
			serviceTable.setSpacingBefore(20);

			serviceTable.addCell(cell("Description", headerFont));
			serviceTable.addCell(cell("Amount", headerFont));

			serviceTable.addCell(cell("Property Consulting Fee", normalFont));
			serviceTable.addCell(cell("₹ " + payment.getAmount(), normalFont));

			document.add(serviceTable);

			// 🔹 Payment Info
			PdfPTable payTable = new PdfPTable(2);
			payTable.setWidthPercentage(100);
			payTable.setSpacingBefore(15);

			payTable.addCell(cell("Payment Mode:", headerFont));
			payTable.addCell(cell("Razorpay", normalFont));

			//payTable.addCell(cell("Payment ID:", headerFont));
			//payTable.addCell(cell(payment.getRazorpayPaymentId(), normalFont));

			payTable.addCell(cell("Status:", headerFont));
			payTable.addCell(cell(payment.getStatus(), normalFont));

			document.add(payTable);

			document.add(new Paragraph("\nThis is a system generated invoice. No signature required.",
					new Font(Font.HELVETICA, 9, Font.ITALIC)));

			document.close();
		} catch (Exception e) {
			throw new RuntimeException("Invoice generation failed", e);
		}

		return out.toByteArray();
	}

	private PdfPCell cell(String text, Font font) {
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		cell.setBorder(Rectangle.NO_BORDER);
		cell.setPadding(6);
		return cell;
	}
}

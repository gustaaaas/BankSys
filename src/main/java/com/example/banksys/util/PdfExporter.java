package com.example.banksys.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.List;

public class PdfExporter {

    public static void exportToPDF(List<Double> payments, String fileName) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            document.add(new Paragraph("Paskolos rezultatai", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.addCell("Mėnuo");
            table.addCell("Įmoka (€)");

            for (int i = 0; i < payments.size(); i++) {
                table.addCell(String.valueOf(i + 1));
                table.addCell(String.format("%.2f €", payments.get(i)));
            }

            document.add(table);
            document.close();
            System.out.println("PDF sukurtas: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

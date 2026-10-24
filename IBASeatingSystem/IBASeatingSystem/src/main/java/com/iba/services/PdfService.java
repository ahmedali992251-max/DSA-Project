package com.iba.services;

import com.iba.models.Room;
import com.iba.models.Student;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfService {

    private void addIbaHeader(Document doc, String reportTitle) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        // IBA Styled Banner
        Font bannerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
        PdfPCell bannerCell = new PdfPCell(new Phrase("IBA UNIVERSITY - EXAMINATION DEPARTMENT", bannerFont));
        bannerCell.setBackgroundColor(new BaseColor(15, 23, 42)); // Navy Blue
        bannerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        bannerCell.setPadding(12);
        bannerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(bannerCell);

        Font subFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(128, 0, 32)); // Maroon
        PdfPCell subCell = new PdfPCell(new Phrase(reportTitle, subFont));
        subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        subCell.setPadding(8);
        subCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(subCell);

        doc.add(headerTable);
        doc.add(new Paragraph(" "));
    }

    // Module 7: Seating Plan PDF
    public void exportSeatingPlanPdf(List<Room> rooms, File file) throws Exception {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        addIbaHeader(doc, "OFFICIAL EXAMINATION SEATING PLAN");

        for (Room room : rooms) {
            Font roomFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
            doc.add(new Paragraph("Room: " + room.getRoomName() + " (Occupied: " + room.getOccupiedCount() + "/" + room.getCapacity() + ")", roomFont));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            String[] headers = {"Seat Code", "Row / Col", "Student ID", "Student Name", "Batch / Course"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
                c.setBackgroundColor(new BaseColor(30, 58, 138));
                c.setPadding(6);
                table.addCell(c);
            }

            Student[][] grid = room.getSeats();
            for (int r = 0; r < room.getRows(); r++) {
                for (int c = 0; c < room.getCols(); c++) {
                    Student s = grid[r][c];
                    if (s != null) {
                        table.addCell("R" + (r + 1) + "-S" + (c + 1));
                        table.addCell("Row " + (r + 1) + ", Col " + (c + 1));
                        table.addCell(s.getStudentId());
                        table.addCell(s.getName());
                        table.addCell(s.getBatch() + " - " + s.getCourse());
                    }
                }
            }
            doc.add(table);
            doc.add(new Paragraph(" "));
        }
        doc.close();
    }

    // Module 8: Room-wise Attendance Sheet PDF with Signature Space
    public void exportAttendanceSheetPdf(List<Room> rooms, File file) throws Exception {
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            if (i > 0) doc.newPage();

            addIbaHeader(doc, "EXAMINATION ATTENDANCE SHEET - ROOM: " + room.getRoomName());

            PdfPTable table = new PdfPTable(new float[]{1.2f, 1.5f, 3f, 2f, 2.5f});
            table.setWidthPercentage(100);
            String[] headers = {"Seat", "Student ID", "Student Name", "Batch", "Student Signature"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
                c.setBackgroundColor(new BaseColor(128, 0, 32)); // IBA Maroon
                c.setPadding(6);
                table.addCell(c);
            }

            Student[][] grid = room.getSeats();
            for (int r = 0; r < room.getRows(); r++) {
                for (int c = 0; c < room.getCols(); c++) {
                    Student s = grid[r][c];
                    if (s != null) {
                        table.addCell("R" + (r + 1) + "-S" + (c + 1));
                        table.addCell(s.getStudentId());
                        table.addCell(s.getName());
                        table.addCell(s.getBatch());
                        PdfPCell signCell = new PdfPCell(new Phrase(" "));
                        signCell.setFixedHeight(22f); // Space for signature
                        table.addCell(signCell);
                    }
                }
            }
            doc.add(table);
            doc.add(new Paragraph("\nInvigilator Name: _______________________      Signature: _______________________"));
        }
        doc.close();
    }
}
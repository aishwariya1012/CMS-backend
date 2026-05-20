package com.example.demo.Service;


import com.example.demo.Model.Incident;
import com.example.demo.Model.IncidentDetail;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfService {

    @Value("${file.reports-dir}")
    private String reportsDir;

    @Value("${file.status-dir}")
    private String statusDir;

    private static final Font   TITLE_FONT  = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font   HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
    private static final Font   LABEL_FONT  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
    private static final Font   VALUE_FONT  = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font   FOOTER_FONT = new Font(Font.FontFamily.HELVETICA, 8,  Font.ITALIC, BaseColor.GRAY);
    private static final BaseColor BLUE     = new BaseColor(30, 100, 180);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── INCIDENT REPORT PDF ──────────────────────────────────────────

    public String generateIncidentReport(Incident incident, IncidentDetail detail) {
        try {
            Files.createDirectories(Paths.get(reportsDir));
            String filePath = reportsDir + "/" + incident.getUniqueCode() + "-report.pdf";

            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            addTitle(doc, "CRIME MANAGEMENT SYSTEM", "INCIDENT REPORT");
            addSection(doc, "Incident Information");
            addRow(doc, "Unique Code",    incident.getUniqueCode());
            addRow(doc, "Incident Type",  fmt(incident.getIncidentType()));
            addRow(doc, "Status",         incident.getStatus());
            addRow(doc, "Filed On",       incident.getReportedAt() != null ? incident.getReportedAt().format(FMT) : "N/A");
            addRow(doc, "Description",    incident.getDescription());

            addSection(doc, "Complainant Details");
            if (incident.getReportedByUser() != null) {
                addRow(doc, "Name",  incident.getReportedByUser().getFullName());
                addRow(doc, "Email", incident.getReportedByUser().getEmail());
                addRow(doc, "Phone", nvl(incident.getReportedByUser().getPhoneNumber()));
            }

            if (detail != null) {
                addSection(doc, "Incident Details");
                if (detail.getPropertyDescription() != null) addRow(doc, "Property Description", detail.getPropertyDescription());
                if (detail.getValueEstimate()       != null) addRow(doc, "Value Estimate (Rs.)",  String.valueOf(detail.getValueEstimate()));
                if (detail.getLocation()            != null) addRow(doc, "Location",               detail.getLocation());
                if (detail.getLostLocation()        != null) addRow(doc, "Lost At",                detail.getLostLocation());
                if (detail.getLostDate()            != null) addRow(doc, "Lost Date",              detail.getLostDate());
                if (detail.getDamagedPropertyType() != null) addRow(doc, "Damaged Property",      detail.getDamagedPropertyType());
                if (detail.getDamageEstimate()      != null) addRow(doc, "Damage Estimate (Rs.)",  String.valueOf(detail.getDamageEstimate()));
            }

            addFooter(doc, "System-generated document. Please keep this for your records.");
            doc.close();

            log.info("Incident report PDF generated: {}", filePath);
            return filePath;
        } catch (DocumentException | IOException e) {
            log.error("PDF generation failed: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }

    // ── STATUS CARD PDF ──────────────────────────────────────────────

    public String generateStatusCard(Incident incident) {
        try {
            Files.createDirectories(Paths.get(statusDir));
            String filePath = statusDir + "/" + incident.getUniqueCode() + "-status.pdf";

            Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            addTitle(doc, "CRIME MANAGEMENT SYSTEM", "CASE STATUS CARD");
            addSection(doc, "Case Information");
            addRow(doc, "Unique Code",   incident.getUniqueCode());
            addRow(doc, "Incident Type", fmt(incident.getIncidentType()));
            addRow(doc, "Final Status",  incident.getStatus());
            addRow(doc, "Filed On",      incident.getReportedAt() != null ? incident.getReportedAt().format(FMT) : "N/A");
            addRow(doc, "Closed On",     incident.getUpdatedAt()  != null ? incident.getUpdatedAt().format(FMT)  : "N/A");

            addSection(doc, "Complainant");
            if (incident.getReportedByUser() != null) {
                addRow(doc, "Name",  incident.getReportedByUser().getFullName());
                addRow(doc, "Email", incident.getReportedByUser().getEmail());
            }

            addSection(doc, "Investigating Officer");
            if (incident.getAssignedOfficer() != null) {
                addRow(doc, "Name",  incident.getAssignedOfficer().getFullName());
                addRow(doc, "Badge", incident.getAssignedOfficer().getBadgeNumber());
            } else {
                addRow(doc, "Officer", "Not assigned");
            }

            addSection(doc, "Verification");
            if (incident.getVerifiedByHead() != null) {
                addRow(doc, "Verified By", incident.getVerifiedByHead().getFullName());
            }

            addFooter(doc, "Case has been verified and closed. This is your official status card.");
            doc.close();

            log.info("Status card PDF generated: {}", filePath);
            return filePath;
        } catch (DocumentException | IOException e) {
            log.error("Status card PDF generation failed: {}", e.getMessage());
            throw new RuntimeException("PDF generation failed: " + e.getMessage());
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private void addTitle(Document doc, String title, String subtitle) throws DocumentException {
        Paragraph t = new Paragraph(title, TITLE_FONT);
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);

        Font sf = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, BLUE);
        Paragraph s = new Paragraph(subtitle, sf);
        s.setAlignment(Element.ALIGN_CENTER);
        s.setSpacingAfter(12f);
        doc.add(s);

        LineSeparator line = new LineSeparator();
        line.setLineColor(BLUE);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private void addSection(Document doc, String title) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        PdfPCell cell = new PdfPCell(new Phrase(title, HEADER_FONT));
        cell.setBackgroundColor(BLUE);
        cell.setPadding(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        doc.add(table);
    }

    private void addRow(Document doc, String label, String value) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{2, 4});
        table.setWidthPercentage(100);
        PdfPCell lc = new PdfPCell(new Phrase(label, LABEL_FONT));
        lc.setPadding(5f);
        lc.setBorderColor(BaseColor.LIGHT_GRAY);
        PdfPCell vc = new PdfPCell(new Phrase(value != null ? value : "N/A", VALUE_FONT));
        vc.setPadding(5f);
        vc.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(lc);
        table.addCell(vc);
        doc.add(table);
    }

    private void addFooter(Document doc, String text) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        doc.add(new Chunk(line));
        Paragraph footer = new Paragraph(text, FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(5f);
        doc.add(footer);
    }

    private String fmt(String type) {
        return type == null ? "N/A" : type.replace("_", " ");
    }

    private String nvl(String s) {
        return s == null ? "N/A" : s;
    }
}

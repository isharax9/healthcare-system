package com.globemed.utils;

import com.globemed.billing.MedicalBill;
import com.globemed.insurance.InsurancePlan;
import com.globemed.patient.PatientRecord;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File; // <-- NEW IMPORT
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillPrinter {

    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_HEADER_BOLD = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
    private static final Font FONT_FINAL_AMOUNT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
    private static final Font FONT_ITALIC = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

    public static void printBill(MedicalBill bill, PatientRecord patient, List<InsurancePlan> allPlans) {
        String folderName = "BillingReports"; // Define the specific folder name

        // --- NEW: Create a folder if it doesn't exist ---
        File folder = new File(folderName);
        if (!folder.exists()) {
            if (!folder.mkdirs()) { // Use mkdirs to create parent directories if they don't exist
                System.err.println("Failed to create directory: " + folderName);
                return; // Abort if the folder can't be created
            }
        }

        // Combine folder path and file name
        String fileNameOnly = String.format("Bill-%d-%s-%s.pdf", bill.getBillId(), patient.getPatientId(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        String fullPath = folder.getAbsolutePath() + File.separator + fileNameOnly; // Combine folder and file name

        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(fullPath)); // --- MODIFIED: Use fullPath ---
            document.open();

            addHeader(document, bill, patient);

            addFinancials(document, bill, patient);

            addInsuranceLegend(document, patient.getInsurancePlan(), allPlans);

            addFooter(document);

            System.out.println("PDF Bill generated: " + fullPath); // --- MODIFIED: Print full path ---

        } catch (DocumentException | IOException e) {
            System.err.println("Error generating PDF bill: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static void addHeader(Document document, MedicalBill bill, PatientRecord patient) throws DocumentException {
        Paragraph title = new Paragraph("GlobeMed Healthcare - Official Bill", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        String printDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        addBoldNormalPair(document, "Printed on: ", printDateTime);
        document.add(Chunk.NEWLINE);

        addBoldNormalPair(document, "Bill ID: ", String.valueOf(bill.getBillId()));
        addBoldNormalPair(document, "Patient ID: ", patient.getPatientId());
        addBoldNormalPair(document, "Patient Name: ", patient.getName());
        document.add(Chunk.NEWLINE);
        addBoldNormalPair(document, "Requested Service: ", bill.getServiceDescription());
        document.add(Chunk.NEWLINE);
    }

    private static void addFinancials(Document document, MedicalBill bill, PatientRecord patient) throws DocumentException {
        document.add(new LineSeparator());
        document.add(new Paragraph(" "));
        addBoldNormalPair(document, "Original Amount: ", String.format("$%.2f", bill.getAmount()));

        InsurancePlan plan = patient.getInsurancePlan();
        if (plan != null) {
            double discount = bill.getAmount() - bill.getFinalAmount();
            String insuranceLine = String.format("Insurance Discount (%s - %.0f%%):", plan.getPlanName(), plan.getCoveragePercent());
            addBoldNormalPair(document, insuranceLine, String.format("-$%.2f", discount));
        }

        document.add(new Paragraph(" "));

        Paragraph finalAmount = new Paragraph();
        finalAmount.add(new Chunk("Final Amount Due: ", FONT_FINAL_AMOUNT));
        finalAmount.add(new Chunk(String.format("$%.2f", bill.getFinalAmount()), FONT_FINAL_AMOUNT));
        finalAmount.setAlignment(Element.ALIGN_RIGHT);
        document.add(finalAmount);
        document.add(Chunk.NEWLINE);
    }

    private static void addInsuranceLegend(Document document, InsurancePlan appliedPlan, List<InsurancePlan> allPlans) throws DocumentException {
        document.add(new LineSeparator());
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Insurance Plan Coverage Rates", FONT_HEADER_BOLD));
        for (InsurancePlan plan : allPlans) {
            Phrase planLine = new Phrase();
            String text = String.format("- %s Plan: %.0f%% Coverage", plan.getPlanName(), plan.getCoveragePercent());
            planLine.add(new Chunk(text, FONT_NORMAL));
            if (appliedPlan != null && appliedPlan.getPlanId() == plan.getPlanId()) {
                planLine.add(new Chunk(" (Your Plan)", FONT_HEADER_BOLD));
            }
            document.add(new Paragraph(planLine));
        }
    }

    private static void addFooter(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Thank you for choosing GlobeMed Healthcare.", FONT_ITALIC);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private static void addBoldNormalPair(Document doc, String boldText, String normalText) throws DocumentException {
        Phrase phrase = new Phrase();
        phrase.add(new Chunk(boldText, FONT_HEADER_BOLD));
        phrase.add(new Chunk(normalText, FONT_NORMAL));
        doc.add(new Paragraph(phrase));
    }
}
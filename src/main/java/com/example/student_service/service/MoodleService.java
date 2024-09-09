package com.example.student_service.service;

import com.example.student_service.model.Student;
import com.example.student_service.util.IdentifierGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoodleService {
    @Autowired
    private IdentifierGenerator identifierGenerator;
    public List<Student> importStudentsFromExcel(InputStream inputStream) {
        List<Student> students = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String firstName, lastName, loginMoodle, passwordMoodle;
                    passwordMoodle = identifierGenerator.generatePassword();

                    if (row.getCell(0) == null) {
                        lastName = row.getCell(1).getStringCellValue().replaceAll("^\\s+", "");
                        if (lastName.split(" ").length > 1)
                            loginMoodle = identifierGenerator.generateLogin(lastName.split(" ")[0], lastName.split(" ")[1]);
                        else
                            loginMoodle = identifierGenerator.generateLogin(lastName.split(" ")[0], lastName.split(" ")[0]);
                        Student student = new Student(null, lastName, loginMoodle, passwordMoodle);
                        students.add(student);
                    }
                    else if (row.getCell(1) == null) {
                        firstName = row.getCell(0).getStringCellValue().replaceAll("^\\s+", "");
                        if (firstName.split(" ").length > 1)
                            loginMoodle = identifierGenerator.generateLogin(firstName.split(" ")[0], firstName.split(" ")[1]);
                        else
                            loginMoodle = identifierGenerator.generateLogin(firstName.split(" ")[0], firstName.split(" ")[0]);
                        Student student = new Student(firstName, null, loginMoodle, passwordMoodle);
                        students.add(student);
                    }
                    else {
                        firstName = row.getCell(0).getStringCellValue().replaceAll("^\\s+", "");
                        lastName = row.getCell(1).getStringCellValue().replaceAll("^\\s+", "");
                        loginMoodle = identifierGenerator.generateLogin(firstName.split(" ")[0], lastName.split(" ")[0]);
                        Student student = new Student(firstName, lastName, loginMoodle, passwordMoodle);
                        students.add(student);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    public byte[] generateExportFile(List<Student> students, String format) throws IOException {
        if (format.equalsIgnoreCase("pdf")) {
            return generatePdfFile(students);
        } else {
            return generateExcelFile(students);
        }
    }

    public byte[] generatePdfFile(List<Student> students) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Titre
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 14);
                contentStream.newLineAtOffset(150, 750);
                contentStream.showText("Liste des ID Moodle étudiants - SJI");
                contentStream.endText();

                // Date
                LocalDate today = LocalDate.now();
                String formattedDate = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                contentStream.newLineAtOffset(450, 730);
                contentStream.showText("Date: " + formattedDate);
                contentStream.endText();

                // Tableau
                float margin = 25;
                float yStart = 700;
                float tableWidth = 550;
                float rowHeight = 20;
                float cellMargin = 5;
                String[] headers = {"N°", "Nom", "Prénom", "Identifiant Moodle", "Mot de passe"};
                int rows = students.size() + 1; // +1 pour l'en-tête

                // Dessiner le tableau
                drawTable(contentStream, yStart, margin, tableWidth, rowHeight, rows, headers.length);

                float[] columnWidths = new float[headers.length];
                columnWidths[0] = tableWidth * 0.05f;
                columnWidths[1] = tableWidth * 0.25f;
                columnWidths[2] = tableWidth * 0.20f;
                columnWidths[3] = tableWidth * 0.38f;
                columnWidths[4] = tableWidth * 0.12f;

                // En-têtes
                float nextY = yStart - rowHeight / 2;
                float nextX = margin + cellMargin;
                for (int i = 0; i < headers.length; i++) {
                    writeInCell(contentStream, nextX, nextY, headers[i]);
                    nextX += columnWidths[i];
                }

                // Données des étudiants
                int studentNumber = 1;
                for (Student student : students) {
                    nextY -= rowHeight;
                    nextX = margin + cellMargin;
                    writeInCell(contentStream, nextX, nextY, String.valueOf(studentNumber++));
                    nextX += columnWidths[0];
                    writeInCell(contentStream, nextX, nextY, student.getLastName());
                    nextX += columnWidths[1];
                    writeInCell(contentStream, nextX, nextY, student.getFirstName());
                    nextX += columnWidths[2];
                    writeInCell(contentStream, nextX, nextY, student.getIdentifier());
                    nextX += columnWidths[3];
                    writeInCell(contentStream, nextX, nextY, student.getPassword());
                }
            }
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void drawTable(PDPageContentStream contentStream, float y, float margin, float width, float rowHeight, int rows, int cols) throws IOException {
        float nextY = y;
        // Dessiner les lignes horizontales
        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(margin, nextY);
            contentStream.lineTo(margin + width, nextY);
            contentStream.stroke();
            nextY -= rowHeight;
        }

        // Calculer les largeurs des colonnes
        float[] columnWidths = new float[cols];
        columnWidths[0] = width * 0.05f; // 5% pour la 1re colonne
        columnWidths[1] = width * 0.25f; // 20% pour la 2e colonne
        columnWidths[2] = width * 0.20f; // 5% pour la 3e colonne
        columnWidths[3] = width * 0.38f; // 20% pour la 4e colonne
        columnWidths[4] = width * 0.12f; // 20% pour la 5e colonne

        // Dessiner les lignes verticales
        float nextX = margin;
        for (int i = 0; i <= cols; i++) {
            contentStream.moveTo(nextX, y);
            contentStream.lineTo(nextX, y - rowHeight * rows);
            contentStream.stroke();
            if (i < cols) {
                nextX += columnWidths[i];
            }
        }
    }

    private void writeInCell(PDPageContentStream contentStream, float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 10);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text != null ? text : "");  // Gestion des valeurs null
        contentStream.endText();
    }

    private byte[] generateExcelFile(List<Student> students) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("MoodleEtudiants");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Prénom");
        headerRow.createCell(1).setCellValue("Nom");
        headerRow.createCell(2).setCellValue("Identifiant Moodle");
        headerRow.createCell(3).setCellValue("Mot de passe");
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getFirstName());
            row.createCell(1).setCellValue(student.getLastName());
            row.createCell(2).setCellValue(student.getIdentifier());
            row.createCell(3).setCellValue(student.getPassword());
        }
        //  Ajuster la largeur des colonnes
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        return baos.toByteArray();
    }
}
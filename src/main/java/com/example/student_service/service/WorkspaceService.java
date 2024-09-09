package com.example.student_service.service;

import com.example.student_service.model.Student;
import com.example.student_service.util.IdentifierGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class WorkspaceService {
    @Autowired
    private IdentifierGenerator identifierGenerator;

    public List<Student> importStudentsFromExcel(InputStream inputStream) {
        List<Student> students = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String firstName, lastName, formattedIdentifier;
                    String password = identifierGenerator.generatePassword();
                    if (row.getCell(0) == null) {
                        lastName = row.getCell(1).getStringCellValue().replaceAll("^\\s+", "");
                        if (lastName.split(" ").length > 1)
                            formattedIdentifier = identifierGenerator.generateIdentifier(lastName.split(" ")[0], lastName.split(" ")[1]);
                        else
                            formattedIdentifier = identifierGenerator.generateIdentifier(lastName.split(" ")[0], lastName.split(" ")[0]);
                        Student student = new Student(null, lastName, formattedIdentifier, password);
                        students.add(student);
                    }
                    else if (row.getCell(1) == null) {
                        firstName = row.getCell(0).getStringCellValue().replaceAll("^\\s+", "");
                        if (firstName.split(" ").length > 1)
                            formattedIdentifier = identifierGenerator.generateIdentifier(firstName.split(" ")[0], firstName.split(" ")[1]);
                        else
                            formattedIdentifier = identifierGenerator.generateIdentifier(firstName.split(" ")[0], firstName.split(" ")[0]);
                        Student student = new Student(firstName, null, formattedIdentifier, password);
                        students.add(student);
                    }
                    else {
                        firstName = row.getCell(0).getStringCellValue().replaceAll("^\\s+", "");
                        lastName = row.getCell(1).getStringCellValue().replaceAll("^\\s+", "");
                        formattedIdentifier = identifierGenerator.generateIdentifier(firstName.split(" ")[0], lastName.split(" ")[0]);
                        Student student = new Student(firstName, lastName, formattedIdentifier, password);
                        students.add(student);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    public List<byte[]> generateWorkspacePdfFiles(List<Student> students, String groupName) throws IOException {
        List<byte[]> pdfFiles = new ArrayList<>();
        Map<String, List<List<String>>> tables = generateStudentTables(students, groupName);

        for (Map.Entry<String, List<List<String>>> entry : tables.entrySet()) {
            System.out.println("entry.getKey() "+entry.getKey());
            pdfFiles.add(generateSinglePdfFile(entry.getValue(), entry.getKey()));
        }

        return pdfFiles;
    }

    private static final float IMAGE_SCALE = 0.3f;
    private static final float TITLE_OFFSET_X = 50;
    private static final float TITLE_OFFSET_Y = 670;
    private static final float TABLE_START_Y = 620;
    private static final float CELL_PADDING = 2;
    private static final float TEXT_OFFSET_Y = 10;

    private static final float PAGE_WIDTH = 850;  // Largeur pour le format paysage
    private static final float PAGE_HEIGHT = 1500; // Hauteur pour le format paysage

    private byte[] generateSinglePdfFile(List<List<String>> tableData, String tableName) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Créer une page avec les dimensions spécifiées
            PDPage page = new PDPage(new PDRectangle(PAGE_WIDTH, PAGE_HEIGHT));
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Ajouter l'image
                ClassPathResource resource = new ClassPathResource("static/imgs/logoIsj.png");
                PDImageXObject image = PDImageXObject.createFromFile(resource.getFile().getAbsolutePath(), document);

                contentStream.drawImage(image, 50, PAGE_HEIGHT-60, image.getWidth() * IMAGE_SCALE, image.getHeight() * IMAGE_SCALE);

                // Titre
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(TITLE_OFFSET_X, PAGE_HEIGHT-80);
                String nomTableau = tableName.equals("table1") ? "identifiants Workspace" : "identifiants de groupe";
                contentStream.showText("Liste des " + nomTableau);
                contentStream.endText();

                // Dessiner le tableau
                drawTable(contentStream, tableData, 30, PAGE_HEIGHT-100, 800, 20);
            }

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    private void drawTable(PDPageContentStream contentStream, List<List<String>> tableData, float x, float y, float width, float rowHeight) throws IOException {
        int rows = tableData.size();
        int cols = tableData.get(0).size();
        float cellWidth = width / cols;

        for (int i = 0; i <= rows; i++) {
            contentStream.moveTo(x, y - i * rowHeight);
            contentStream.lineTo(x + width, y - i * rowHeight);
            contentStream.stroke();
        }

        for (int i = 0; i <= cols; i++) {
            contentStream.moveTo(x + i * cellWidth, y);
            contentStream.lineTo(x + i * cellWidth, y - rows * rowHeight);
            contentStream.stroke();
        }

        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String text = tableData.get(i).get(j);
                contentStream.beginText();
                contentStream.newLineAtOffset(x + j * cellWidth + CELL_PADDING, y - (i + 1) * rowHeight + TEXT_OFFSET_Y);
                contentStream.showText(text != null ? text : "");
                contentStream.endText();
            }
        }
    }

    public List<byte[]> generateWorkspaceExcelFiles(List<Student> students, String groupName) throws IOException {
        List<byte[]> excelFiles = new ArrayList<>();
        Map<String, List<List<String>>> tables = generateStudentTables(students, groupName);

        for (Map.Entry<String, List<List<String>>> entry : tables.entrySet()) {
            excelFiles.add(generateSingleExcelFile(entry.getValue(), entry.getKey()));
        }

        return excelFiles;
    }

    private byte[] generateSingleExcelFile(List<List<String>> tableData, String tableName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(tableName);

            // Décaler les données vers le bas pour laisser de la place à un éventuel en-tête
            int rowOffset = 0;

            // Vérification des données du tableau
            if (tableData != null && !tableData.isEmpty()) {
                for (int i = 0; i < tableData.size(); i++) {
                    Row row = sheet.createRow(i + rowOffset);
                    for (int j = 0; j < tableData.get(i).size(); j++) {
                        row.createCell(j).setCellValue(tableData.get(i).get(j) != null ? tableData.get(i).get(j) : "");
                    }
                }

                // Ajustement de la largeur des colonnes
                for (int i = 0; i < tableData.get(0).size(); i++) {
                    sheet.autoSizeColumn(i);
                }
            } else {
                System.err.println("Aucune donnée à écrire dans le fichier Excel.");
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }


    public Map<String, List<List<String>>> generateStudentTables(List<Student> students, String groupName) {
        List<List<String>> table1 = new ArrayList<>();
        List<List<String>> table2 = new ArrayList<>();

        // Créer le premier tableau
        List<String> header1 = Arrays.asList("First Name [Required]", "Last Name [Required]", "Email Address [Required]",
                "Password [Required]", "Change Password at Next Sign-In");
        table1.add(header1);

        for (Student student : students) {
            List<String> row = Arrays.asList(student.getFirstName(), student.getLastName(), student.getIdentifier(),
                    student.getPassword(), "YES");
            table1.add(row);
        }

        // Créer le second tableau
        List<String> header2 = Arrays.asList("Group Email [Required]", "Member Email [Required]",
                "Member Type", "Member Role");
        table2.add(header2);

        for (Student student : students) {
            List<String> row = Arrays.asList(groupName + "@institutsaintjean.org", student.getIdentifier(), "USER", "MEMBER");
            table2.add(row);
        }

        Map<String, List<List<String>>> tables = new HashMap<>();
        tables.put("table1", table1);
        tables.put("table2", table2);

        return tables;
    }
}
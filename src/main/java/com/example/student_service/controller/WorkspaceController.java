package com.example.student_service.controller;


import com.example.student_service.model.Student;
import com.example.student_service.service.WorkspaceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/workspace")
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;
    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping("")
    public String showMainPage() {
        return "main";
    }

    @GetMapping("/fragment/{type}/{id}")
    public String getFragment(@PathVariable String type, @PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("uploadType", type);
        if ("preview".equals(type)) {
            List<Student> students = (List<Student>) session.getAttribute("students");
            if (students == null) {
                students = new ArrayList<>();
            }
            model.addAttribute("students", students);
            model.addAttribute("type", determineType(id));
        }
        System.out.println("model " + model);
        System.out.println("getFragment " + type );
        return type;
    }

    @PostMapping("/group-name")
    @ResponseBody
    public String handleGroupName(@RequestParam("groupName") String groupName, HttpSession session) {
        try {
            // Logique pour gérer le nom du groupe
            System.out.println("groupName " + groupName);
            // Par exemple, vous pouvez l'ajouter à la session ou à une base de données
            session.setAttribute("groupName", groupName);
            return "Nom de groupe enregistré : " + groupName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'enregistrement du nom de groupe : " + e.getMessage();
        }
    }

    @PostMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam(value = "type", required = false) String type,
                                   @RequestParam("file") MultipartFile file,
                                   Model model,
                                   HttpSession session) {

        try {
            List<Student> students = workspaceService.importStudentsFromExcel(file.getInputStream());
            session.setAttribute("students", students);
            model.addAttribute("students", students);
            model.addAttribute("type", type);
            System.out.println("type "+type);

            String groupName = (String) session.getAttribute("groupName");
            Map<String, List<List<String>>> tables = workspaceService.generateStudentTables(students, groupName);

            model.addAttribute("tables", tables);
            return loadFragment("preview", model);
        } catch (IOException e) {
            e.printStackTrace();
            return "Erreur lors du chargement du fichier : " + e.getMessage();
        }
    }


    @GetMapping("/download/{format}/{type}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String format,
                                               @PathVariable String type,
                                               HttpSession session) {
        List<Student> students = (List<Student>) session.getAttribute("students");
        String groupName = (String) session.getAttribute("groupName");
        if (students == null) {
            students = new ArrayList<>();
        }
        System.out.println("groupName "+groupName);
        try {
            if ("workspace".equals(type)) {
                if ("pdf".equalsIgnoreCase(format)) {
                    System.out.println("format "+format);
                    List<byte[]> pdfFiles = workspaceService.generateWorkspacePdfFiles(students, groupName);
                    return createZipResponse(pdfFiles, "workspace_pdf_files.zip");
                } else if ("xlsx".equalsIgnoreCase(format)) {
                    List<byte[]> excelFiles = workspaceService.generateWorkspaceExcelFiles(students, groupName);
                    return createZipResponse(excelFiles, "workspace_excel_files.zip");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.badRequest().build();
    }

    private ResponseEntity<byte[]> createZipResponse(List<byte[]> files, String zipFileName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        for (int i = 0; i < files.size(); i++) {
            // Utilisez l'extension appropriée en fonction de la demande
            String fileExtension = zipFileName.contains("pdf") ? ".pdf" : ".xlsx";
            String entryName = (i == 0) ? "Liste des identifiants de groupe" + fileExtension : "Liste des adresses institutionnelles" + fileExtension;
            ZipEntry entry = new ZipEntry(entryName + fileExtension);
            zos.putNextEntry(entry);
            zos.write(files.get(i));
            zos.closeEntry();
        }

        zos.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", zipFileName);

        return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);
    }

    private String loadFragment(String fragmentName, Model model) {
        System.out.println("loadFragment" + model);
        Context context = new Context();
        context.setVariables(model.asMap());
        return templateEngine.process(fragmentName, context);
    }

    private String determineType(Long id) {
        switch (id.intValue()) {
            case 1:
                return "workspace";
            case 2:
                return "moodle";
            case 3:
                return "matricule";
            case 4:
                return "portail_captif";
            default:
                return "unknown";
        }
    }
}
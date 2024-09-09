package com.example.student_service.controller;

import com.example.student_service.model.Student;
import com.example.student_service.service.MatriculeService;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/matricule")
public class MatriculeController {

    @Autowired
    private MatriculeService matriculeService;

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
        return type;
    }

    @PostMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam(value = "type", required = false) String type,
                                   @RequestParam("file") MultipartFile file,
                                   Model model,
                                   HttpSession session) {
        try {
            List<Student> students = matriculeService.importStudentsFromExcel(file.getInputStream());
            session.setAttribute("students", students);
            model.addAttribute("students", students);
            model.addAttribute("type", type);
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
        if (students == null) {
            students = new ArrayList<>();
        }
        try {
            byte[] fileContent = matriculeService.generateExportFile(students, format);
            HttpHeaders headers = new HttpHeaders();
            String filename = "ListeMatricules." + (format.equalsIgnoreCase("pdf") ? "pdf" : "xlsx");

            if (format.equalsIgnoreCase("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            headers.setContentDispositionFormData("attachment", filename);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

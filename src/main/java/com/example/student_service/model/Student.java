package com.example.student_service.model;

import lombok.*;

@Data
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private String firstName;
    private String lastName;
    private String identifier;
    private String password;
    private String matricule;
//    private String loginMoodle;
//    private String passwordMoodle;


    public Student(String firstName, String lastName, String identifier, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.identifier = identifier;
        this.password = password;
    }

    public Student(String firstName, String lastName, String matricule) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.matricule = matricule;
    }


}


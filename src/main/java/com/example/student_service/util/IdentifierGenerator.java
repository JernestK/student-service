package com.example.student_service.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Calendar;

@Component
public class IdentifierGenerator {
    public String generateIdentifier(String firstName, String lastName) {
        String formattedFirstName = firstName.replaceAll("\\s+", "").replaceAll("à|á|â|ã|ä|å", "a")
                .replaceAll("è|é|ê|ë", "e")
                .replaceAll("ì|í|î|ï", "i")
                .replaceAll("ò|ó|ô|õ|ö", "o")
                .replaceAll("ù|ú|û|ü", "u")
                .replaceAll("ç", "c")
                .replaceAll("'", "");
        String formattedLastName = lastName.replaceAll("\\s+", "").replaceAll("à|á|â|ã|ä|å", "a")
                .replaceAll("è|é|ê|ë", "e")
                .replaceAll("ì|í|î|ï", "i")
                .replaceAll("ò|ó|ô|õ|ö", "o")
                .replaceAll("ù|ú|û|ü", "u")
                .replaceAll("ç", "c")
                .replaceAll("'", "");
        return (formattedFirstName + "." + formattedLastName + "@institutsaintjean.org").toLowerCase();
    }

    public String generatePassword() {
        String capitalLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String smallLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_+";

        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        // On génère un password ayant au moins un caractère de chaque type
        password.append(capitalLetters.charAt(random.nextInt(capitalLetters.length())));
        password.append(smallLetters.charAt(random.nextInt(smallLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // on génère le reste des caractères au hasard
        for (int i = 0; i < 4; i++) {
            String allChars = capitalLetters + smallLetters + numbers + specialChars;
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        return (password.toString());
    }

    public void setCurrentSequenceNumber(int currentSequenceNumber) {
        this.currentSequenceNumber = currentSequenceNumber;
    }
    private int currentSequenceNumber = 1;
    public String generateMatricule() {
        final String MATRICULE_PREFIX = "I";
        final int MATRICULE_LENGTH = 3;

        // Récupérer l'année en cours
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        // Construire le matricule
        StringBuilder matricule = new StringBuilder();
        matricule.append(String.format("%02d", currentYear % 100));
        matricule.append(String.format("%02d", (currentYear + 1) % 100));
        matricule.append(MATRICULE_PREFIX);
        matricule.append(String.format("%0" + MATRICULE_LENGTH + "d", currentSequenceNumber));

        // Incrémenter le numéro de séquence
        currentSequenceNumber++;

        return matricule.toString();
    }

    public String generateLogin(String firstName, String lastName) {
        String formattedFirstName = firstName.replaceAll("\\s+", "").replaceAll("à|á|â|ã|ä|å", "a")
                .replaceAll("è|é|ê|ë", "e")
                .replaceAll("ì|í|î|ï", "i")
                .replaceAll("ò|ó|ô|õ|ö", "o")
                .replaceAll("ù|ú|û|ü", "u")
                .replaceAll("ç", "c")
                .replaceAll("'", "");
        String formattedLastName = lastName.replaceAll("\\s+", "").replaceAll("à|á|â|ã|ä|å", "a")
                .replaceAll("è|é|ê|ë", "e")
                .replaceAll("ì|í|î|ï", "i")
                .replaceAll("ò|ó|ô|õ|ö", "o")
                .replaceAll("ù|ú|û|ü", "u")
                .replaceAll("ç", "c")
                .replaceAll("'", "");
        return (formattedFirstName + "." + formattedLastName).toLowerCase();
    }
}
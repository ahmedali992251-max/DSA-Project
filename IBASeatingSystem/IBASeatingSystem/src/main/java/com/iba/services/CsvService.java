package com.iba.services;

import com.iba.models.Student;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvService {

    public static class ImportResult {
        public List<Student> validStudents = new ArrayList<>();
        public List<String> errors = new ArrayList<>();
    }

    public ImportResult importFromCsv(File file) {
        ImportResult result = new ImportResult();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int rowNum = 0;
            while ((line = br.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                // Header row ko skip karein
                if (rowNum == 1 && parts[0].toLowerCase().contains("id")) continue;

                if (parts.length < 4) {
                    result.errors.add("Row " + rowNum + ": Incomplete columns (Expected: ID, Name, Batch, Course)");
                    continue;
                }

                String id = parts[0].trim();
                String name = parts[1].trim();
                String batch = parts[2].trim();
                String course = parts[3].trim();

                if (id.isEmpty() || name.isEmpty() || batch.isEmpty() || course.isEmpty()) {
                    result.errors.add("Row " + rowNum + ": Missing data field.");
                } else {
                    result.validStudents.add(new Student(id, name, batch, course));
                }
            }
        } catch (Exception e) {
            result.errors.add("Error reading CSV: " + e.getMessage());
        }
        return result;
    }
}
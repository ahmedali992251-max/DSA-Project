package com.iba.services;

import com.iba.models.Room;
import com.iba.models.Student;
import java.util.*;

public class SeatingService {

    // Modules 3 & 4: Distribute students across rooms & ensure same-batch students don't sit adjacent
    public int generateSeating(List<Student> students, List<Room> rooms) {
        for (Room r : rooms) r.clearSeats();

        // Group students by batch so we can alternate batches
        Map<String, Queue<Student>> batchMap = new HashMap<>();
        for (Student s : students) {
            batchMap.computeIfAbsent(s.getBatch(), k -> new LinkedList<>()).add(s);
        }

        int placedCount = 0;

        for (Room room : rooms) {
            Student[][] grid = room.getSeats();
            for (int r = 0; r < room.getRows(); r++) {
                for (int c = 0; c < room.getCols(); c++) {
                    Student chosen = pickSafeStudent(batchMap, grid, r, c);
                    if (chosen != null) {
                        grid[r][c] = chosen;
                        placedCount++;
                    }
                }
            }
        }
        return placedCount;
    }

    private Student pickSafeStudent(Map<String, Queue<Student>> batchMap, Student[][] grid, int r, int c) {
        String leftBatch = (c > 0 && grid[r][c - 1] != null) ? grid[r][c - 1].getBatch() : "";
        String topBatch = (r > 0 && grid[r - 1][c] != null) ? grid[r - 1][c].getBatch() : "";

        // Best case: Find a batch that doesn't match left OR front neighbor
        List<String> keys = new ArrayList<>(batchMap.keySet());
        keys.sort((a, b) -> batchMap.get(b).size() - batchMap.get(a).size()); // Pick largest batch first

        for (String batch : keys) {
            Queue<Student> q = batchMap.get(batch);
            if (!q.isEmpty() && !batch.equalsIgnoreCase(leftBatch) && !batch.equalsIgnoreCase(topBatch)) {
                return q.poll();
            }
        }
        // Fallback if only one batch remains
        for (String batch : keys) {
            Queue<Student> q = batchMap.get(batch);
            if (!q.isEmpty()) return q.poll();
        }
        return null;
    }

    // Module 6: Search Student by ID
    public String searchStudentSeat(String studentId, List<Room> rooms) {
        for (Room room : rooms) {
            Student[][] grid = room.getSeats();
            for (int r = 0; r < room.getRows(); r++) {
                for (int c = 0; c < room.getCols(); c++) {
                    Student s = grid[r][c];
                    if (s != null && s.getStudentId().equalsIgnoreCase(studentId.trim())) {
                        return "FOUND!\n" +
                                "Student ID : " + s.getStudentId() + "\n" +
                                "Name       : " + s.getName() + "\n" +
                                "Batch/Grp  : " + s.getBatch() + " (" + s.getCourse() + ")\n" +
                                "Room       : " + room.getRoomName() + "\n" +
                                "Row        : Row " + (r + 1) + "\n" +
                                "Seat       : Col " + (c + 1) + " (Seat Code: R" + (r + 1) + "-S" + (c + 1) + ")";
                    }
                }
            }
        }
        return "No seat assignment found for Student ID: " + studentId;
    }
}
package com.iba.models;

public class Student {
    private String studentId;
    private String name;
    private String batch;   // Group/Batch (e.g., BCS-1, BBA-2)
    private String course;

    public Student(String studentId, String name, String batch, String course) {
        this.studentId = studentId;
        this.name = name;
        this.batch = batch;
        this.course = course;
    }

    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getBatch() { return batch; }
    public String getCourse() { return course; }
}
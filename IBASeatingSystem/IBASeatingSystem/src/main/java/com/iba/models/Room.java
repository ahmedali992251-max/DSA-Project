package com.iba.models;

public class Room {
    private String roomName;
    private int rows;
    private int cols;
    private Student[][] seats;

    public Room(String roomName, int rows, int cols) {
        this.roomName = roomName;
        this.rows = rows;
        this.cols = cols;
        this.seats = new Student[rows][cols];
    }

    public String getRoomName() { return roomName; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getCapacity() { return rows * cols; }
    public Student[][] getSeats() { return seats; }

    public void clearSeats() {
        this.seats = new Student[rows][cols];
    }

    public int getOccupiedCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (seats[r][c] != null) count++;
            }
        }
        return count;
    }

    @Override
    public String toString() {
        return roomName + " (" + rows + "x" + cols + " - Cap: " + getCapacity() + ")";
    }
}
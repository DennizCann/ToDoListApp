package com.denizcan.model;

import java.time.LocalDate;

public class Task {
    private String name;
    private boolean completed;
    private LocalDate creationDate; // ✅ Yeni ekledik
    private LocalDate dueDate;

    public Task(String name) {
        this.name = name;
        this.completed = false;
        this.creationDate = LocalDate.now(); // Oluşturulma tarihi
        this.dueDate = null;
    }

    // Getter & Setter
    public void setName(String name) { this.name = name; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // Dosyaya yazmak için string formatı
    public String toFileString() {
        return name + "|" + completed + "|" + creationDate + "|" +
                (dueDate != null ? dueDate.toString() : "");
    }

    // Dosyadan string alıp Task oluşturmak için
    public static Task fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 3) return null;

        Task task = new Task(parts[0]);
        task.setCompleted(Boolean.parseBoolean(parts[1]));
        task.creationDate = LocalDate.parse(parts[2]);
        if (parts.length == 4 && !parts[3].isEmpty()) {
            task.setDueDate(LocalDate.parse(parts[3]));
        }
        return task;
    }

    @Override
    public String toString() {
        return (completed ? "[X] " : "[ ] ") + name +
                (dueDate != null ? " (Due: " + dueDate + ")" : "") +
                " (Created: " + creationDate + ")";
    }
}

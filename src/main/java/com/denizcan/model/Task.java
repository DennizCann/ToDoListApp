package com.denizcan.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Task implements Comparable<Task> {
    private String name;
    private boolean completed;
    private LocalDate creationDate; // ✅ Yeni ekledik
    private LocalDate dueDate;
    private UUID id;

    public Task(String name) {
        this.name = name;
        this.completed = false;
        this.creationDate = LocalDate.now(); // Oluşturulma tarihi
        this.dueDate = null;
        this.id = UUID.randomUUID();
    }

    // Getter & Setter
    public void setName(String name) { this.name = name; }

    public void setCompleted(boolean completed) { this.completed = completed; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // Dosyaya yazmak için string formatı
    public String toFileString() {
        return name + "|" + completed + "|" + creationDate + "|" +
                (dueDate != null ? dueDate.toString() : "") + "|" + id;
    }

    // Dosyadan string alıp Task oluşturmak için
    public static Task fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 3) return null;

        Task task = new Task(parts[0]);
        task.setCompleted(Boolean.parseBoolean(parts[1]));
        task.creationDate = LocalDate.parse(parts[2]);
        if (parts.length >= 4 && !parts[3].isEmpty()) {
            task.setDueDate(LocalDate.parse(parts[3]));
        }
        if (parts.length >= 5 && !parts[4].isEmpty()) {
            try {
                task.id = UUID.fromString(parts[4]);
            } catch (IllegalArgumentException ignored) {
                task.id = UUID.randomUUID();
            }
        }
        return task;
    }

    // Konsoldaki gösterimden Task üretir: "[X] Name (Due: 2025-01-01) (Created: 2025-01-01)"
    public static Task fromDisplayString(String line) {
        if (line == null || line.isEmpty()) return null;
        String trimmed = line.trim();

        boolean completedFlag = trimmed.startsWith("[X]");
        boolean pendingFlag = trimmed.startsWith("[ ]");
        if (!completedFlag && !pendingFlag) return null;

        // Durum işaretini kaldır
        String rest = trimmed.substring(3).trim();

        // Created ve Due bilgilerini ayıkla
        LocalDate created = null;
        LocalDate due = null;

        // Created: sondadır
        int createdIdx = rest.lastIndexOf("(Created:");
        if (createdIdx != -1) {
            int end = rest.indexOf(')', createdIdx);
            if (end != -1) {
                String createdStr = rest.substring(createdIdx + "(Created:".length(), end).trim();
                created = LocalDate.parse(createdStr);
                rest = rest.substring(0, createdIdx).trim();
            }
        }

        // Due: varsa Created'tan önce yer alır
        int dueIdx = rest.lastIndexOf("(Due:");
        if (dueIdx != -1) {
            int end = rest.indexOf(')', dueIdx);
            if (end != -1) {
                String dueStr = rest.substring(dueIdx + "(Due:".length(), end).trim();
                due = LocalDate.parse(dueStr);
                rest = rest.substring(0, dueIdx).trim();
            }
        }

        String namePart = rest.trim();
        Task task = new Task(namePart);
        task.setCompleted(completedFlag);
        if (created != null) {
            task.creationDate = created;
        }
        if (due != null) {
            task.setDueDate(due);
        }
        return task;
    }

    @Override
    public String toString() {
        return (completed ? "[X] " : "[ ] ") + name +
                (dueDate != null ? " (Due: " + dueDate + ")" : "") +
                " (Created: " + creationDate + ")";
    }

    @Override
    public int compareTo(Task other) {
        // 0) Completion: incomplete first, completed last
        int byCompleted = Boolean.compare(this.completed, other.completed);
        if (byCompleted != 0) return byCompleted;

        // 1) Due date: null'lar en sonda, en yakın tarih önce
        if (this.dueDate == null && other.dueDate != null) return 1;
        if (this.dueDate != null && other.dueDate == null) return -1;
        if (this.dueDate != null && other.dueDate != null) {
            int byDue = this.dueDate.compareTo(other.dueDate);
            if (byDue != 0) return byDue;
        }

        // 2) Creation date: daha eski önce
        int byCreated = this.creationDate.compareTo(other.creationDate);
        if (byCreated != 0) return byCreated;

        // 3) Name: alfabetik (case-insensitive)
        String thisName = this.name == null ? "" : this.name;
        String otherName = other.name == null ? "" : other.name;
        return thisName.compareToIgnoreCase(otherName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

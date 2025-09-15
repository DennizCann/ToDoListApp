package com.denizcan.util;

import com.denizcan.model.Task;
import com.denizcan.service.TaskService;

import java.io.*;
import java.util.List;

public class FileManager {
    private static final String FILE_NAME = "Tasks.txt";

    // Görevleri dosyaya kaydet (varsayılan: mesaj yazdırılır)
    public static void saveTasks(TaskService taskService) {
        saveTasks(taskService, false);
    }

    // Görevleri dosyaya kaydet (quiet=true iken başarı mesajı bastırılır)
    public static void saveTasks(TaskService taskService, boolean quiet) {
        List<Task> tasks = taskService.getTasksSnapshot();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : tasks) {
                // Konsoldaki görünümle aynı formatta yaz
                writer.write(task.toString());
                writer.newLine();
            }
            if (!quiet) {
                System.out.println("Tasks saved to file successfully!");
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // Dosyadan görevleri yükle
    public static void loadTasks(TaskService taskService) {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Task task;
                if (line.contains("|")) {
                    // Eski pipe-formatı desteği
                    task = Task.fromFileString(line);
                } else {
                    // Konsol görünümü formatı
                    task = Task.fromDisplayString(line);
                }
                if (task != null) {
                    taskService.addTask(task);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }
}

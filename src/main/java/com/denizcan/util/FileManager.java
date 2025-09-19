package com.denizcan.util;

import com.denizcan.model.Task;
import com.denizcan.service.TaskService;
import java.io.*;
import java.util.List;

public class FileManager {
    private static final String LISTS_DIR = "lists";

    public static void ensureListsDir() {
        File dir = new File(LISTS_DIR);
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }

    public static File resolveListFile(String listName) {
        ensureListsDir();
        return new File(LISTS_DIR + File.separator + listName + ".txt");
    }

    public static String[] listAllLists() {
        ensureListsDir();
        File dir = new File(LISTS_DIR);
        String[] names = dir.list((d, name) -> name.toLowerCase().endsWith(".txt"));
        if (names == null) return new String[0];
        for (int i = 0; i < names.length; i++) {
            String n = names[i];
            if (n.endsWith(".txt")) {
                names[i] = n.substring(0, n.length() - 4);
            }
        }
        return names;
    }

    // Görevleri seçili liste dosyasına kaydet (varsayılan: mesaj yazdırılır)
    public static void saveTasks(TaskService taskService, String listName) {
        saveTasks(taskService, listName, false);
    }

    // Görevleri seçili liste dosyasına kaydet (quiet=true iken başarı mesajı bastırılmaz)
    public static void saveTasks(TaskService taskService, String listName, boolean quiet) {
        List<Task> tasks = taskService.getTasksSnapshot();
        File file = resolveListFile(listName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Task task : tasks) {
                // Konsoldaki görünümle aynı formatta yaz
                writer.write(task.toString());
                writer.newLine();
            }
            if (!quiet) {
                System.out.println("Tasks saved to '" + file.getName() + "' successfully!");
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    // Seçili liste dosyasından görevleri yükle
    public static void loadTasks(TaskService taskService, String listName) {
        File file = resolveListFile(listName);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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

package com.denizcan;

import com.denizcan.model.Task;
import com.denizcan.service.TaskService;
import com.denizcan.util.FileManager;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TaskService taskService = new TaskService();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Daha önce kaydedilen görevleri yükle
        FileManager.loadTasks(taskService);

        // Autosave: her 10 saniyede bir görevleri kaydet (sessiz)
        scheduler.scheduleAtFixedRate(() -> FileManager.saveTasks(taskService, true), 10, 10, TimeUnit.SECONDS);

        // Program beklenmedik kapanırsa düzgün kapatmak için shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                scheduler.shutdownNow();
            } catch (Exception ignored) {}
            FileManager.saveTasks(taskService, true);
        }));

        int choice = 0;

        while (choice != 7) {
            showMenu();
            choice = readIntFromOneTo(sc, "Enter a choice (1-7): ", 7);

            switch (choice) {
                case 1:
                    System.out.print("Enter task name: ");
                    String name = sc.nextLine();

                    LocalDate dueDate = readOptionalDueDate(sc);

                    Task newTask = new Task(name);
                    newTask.setDueDate(dueDate);
                    taskService.addTask(newTask);
                    System.out.println("Task added successfully!");
                    break;

                case 2:
                    showTasks(taskService);
                    break;

                case 3:
                    showTasks(taskService);
                    if (taskService.getTasks().isEmpty()) {
                        System.out.println("No tasks to delete.");
                        break;
                    }
                    int delIndex = readIntFromOneTo(sc, "Enter task number to delete: ", taskService.getTasks().size()) - 1;
                    taskService.deleteTask(delIndex);
                    System.out.println("Task deleted if number was valid.");
                    break;

                case 4:
                    showTasks(taskService);
                    if (taskService.getTasks().isEmpty()) {
                        System.out.println("No tasks to edit.");
                        break;
                    }
                    int editIndex = readIntFromOneTo(sc, "Enter task number to edit: ", taskService.getTasks().size()) - 1;
                    System.out.print("Enter new task name: ");
                    String newName = sc.nextLine();
                    taskService.editTask(editIndex, newName);
                    System.out.println("Task updated if number was valid.");
                    break;

                case 5:
                    showTasks(taskService);
                    if (taskService.getTasks().isEmpty()) {
                        System.out.println("No tasks to mark as completed.");
                        break;
                    }
                    int compIndex = readIntFromOneTo(sc, "Enter task number to mark as completed: ", taskService.getTasks().size()) - 1;
                    taskService.markTaskCompleted(compIndex);
                    System.out.println("Task marked completed if number was valid.");
                    break;

                case 6:
                    taskService.sortTasks();
                    System.out.println("Tasks sorted.");
                    break;

                case 7:
                    // Çıkış: autosave'i kapat ve son bir kaydetme yap
                    scheduler.shutdown();
                    try {
                        if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                    FileManager.saveTasks(taskService);
                    System.out.println("Exiting program...");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }

        sc.close();
    }

    public static void showMenu() {
        System.out.println("\n===== TASK MENU =====");
        System.out.println("1. Create a new task");
        System.out.println("2. Show all tasks");
        System.out.println("3. Delete a task");
        System.out.println("4. Edit a task");
        System.out.println("5. Mark task as completed");
        System.out.println("6. Sort tasks");
        System.out.println("7. Save and Exit");
    }

    public static void showTasks(TaskService taskService) {
        if (taskService.getTasks().isEmpty()) {
            System.out.println("No tasks found!");
        } else {
            System.out.println("\nAll Tasks:");
            int i = 1;
            for (Task task : taskService.getTasks()) {
                System.out.println(i + ". " + task);
                i++;
            }
        }
    }

    private static int readIntFromOneTo(Scanner sc, String prompt, int max) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value < 1 || value > max) {
                    System.out.println("Please enter a number between 1 and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static LocalDate readOptionalDueDate(Scanner sc) {
        while (true) {
            System.out.print("Enter due date (YYYY-MM-DD) or leave empty: ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Expected YYYY-MM-DD.");
            }
        }
    }
}
